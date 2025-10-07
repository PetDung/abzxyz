package com.petd.tiktok_system_be.sdk.printSdk.Monkeykingprint;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petd.tiktok_system_be.constant.PrintStatus;
import com.petd.tiktok_system_be.entity.Design.Design;
import com.petd.tiktok_system_be.entity.Order.Address;
import com.petd.tiktok_system_be.entity.Order.Order;
import com.petd.tiktok_system_be.entity.Order.OrderItem;
import com.petd.tiktok_system_be.exception.AppException;
import com.petd.tiktok_system_be.exception.ErrorCode;
import com.petd.tiktok_system_be.repository.OrderRepository;
import com.petd.tiktok_system_be.sdk.SimpleHttpClient;
import com.petd.tiktok_system_be.sdk.printSdk.Monkeykingprint.dto.exception.MKPException;
import com.petd.tiktok_system_be.sdk.printSdk.Monkeykingprint.dto.request.DesignMKP;
import com.petd.tiktok_system_be.sdk.printSdk.Monkeykingprint.dto.request.ItemMPK;
import com.petd.tiktok_system_be.sdk.printSdk.Monkeykingprint.dto.request.OrderData;
import com.petd.tiktok_system_be.sdk.printSdk.Monkeykingprint.dto.request.OrderRequestMKP;
import com.petd.tiktok_system_be.sdk.printSdk.PrintSupplier;
import com.petd.tiktok_system_be.sdk.printSdk.PrinteesHub.dto.response.OrderResponse;
import com.petd.tiktok_system_be.service.ExportConfig.StateMapper;
import com.petd.tiktok_system_be.service.Lib.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.StringUtil;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class MonkeyPrint implements PrintSupplier {
    final SimpleHttpClient httpClient = new SimpleHttpClient();
    final ObjectMapper objectMapper = new ObjectMapper();
    final String baseUrl = "https://monkeykingprint.com";
    String orderId = null;

    final OrderRepository orderRepository;
    final NotificationService notificationService;

    @Override
    public OrderResponse print(Order order) throws IOException {

       try{
           Map<String, String> headers = getHeader();
           String body = buildJson(order);
           log.info(body);
           String api = "/rest/V1/vendors/order/import";

           String response = httpClient.requestForObject(
                   baseUrl + api,
                   "POST",
                   headers,
                   body
           );
           MKPException rest = objectMapper.readValue(response, MKPException.class);

           if(!rest.isSuccess()){
               log.error(rest.getMessage());
               throw new IOException(rest.getMessage());
           }


           log.info("response:\n{}", response);
           return OrderResponse.builder()
                   .orderFulfillId(rest.getOrder().getIncrement_id())
                   .amount(rest.getOrder().getGrand_total())
                   .orderId(orderId)
                   .build();
       }catch (IOException e){
           log.info(e.getMessage());
           throw new AppException(409, e.getMessage());
       }catch (Exception e){
           log.error(e.getMessage());
           throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
       }
    }

    @Override
    public String getCode() {
        return "MKP";
    }

    @Override
    public OrderResponse cancel(Order order) throws IOException {
        order.setPrintStatus(PrintStatus.PRINT_CANCEL.toString());
        orderRepository.save(order);
        notificationService.orderUpdateStatus(order);
        throw new AppException(409, "Nhà in này không hỗ trợ hủy! Hủy trên hệ thống bạn có thể tạo đơn in mới");
    }

    @Override
    public OrderResponse synchronize(Order order) throws IOException {
        return null;
    }

    @Override
    public JsonNode getPrintOrderById(String orderId) throws IOException {
        return null;
    }


    private String getToken () throws IOException {
        String username = "nguyenkhang1000199x@gmail.com";
        String password = "THJ444dbdg$#5j";

        String api ="/rest/V1/integration/customer/token";

        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);

        String bodyJson = objectMapper.writeValueAsString(body);
        return  httpClient.requestForObject(
                baseUrl + api,
                "POST",
                null,
                bodyJson,
                String.class
        );
    }

    private  Map<String, String> getHeader() throws IOException {
        return Map.of(
                "Authorization","Bearer " + getToken(),
                "Accept", "application/json"
        );
    }
    private String buildJson(Order order) throws IOException {
        return  objectMapper.writeValueAsString(buildOrderRequestMKP(order));
    }

    private OrderRequestMKP buildOrderRequestMKP(Order order) throws IOException {

        List<Address> addresses = order.getRecipientAddress().getDistrictInfo();

        Function<String, String> getAddress = level -> addresses.stream()
                .filter(a -> level.equals(a.getAddress_level()))
                .map(Address::getAddress_name)
                .findFirst().orElse(null);


        List<ItemMPK> items = order.getLineItems().stream()
                .filter(orderItem -> orderItem.getIsPrint() == true)
                .collect(Collectors.groupingBy(OrderItem::getSkuId))
                .values().stream()
                .map(orderItems ->{
                    OrderItem orderItem = orderItems.get(0);
                    Design design = orderItem.getDesign();

                    List<DesignMKP> designMKPS = new ArrayList<>();
                    Map<String, String> sideMap = Map.of(
                            "Front", design.getFrontSide(),
                            "Back", design.getBackSide(),
                            "Left", design.getLeftSide(),
                            "Right", design.getRightSide()
                    );
                    sideMap.forEach((side, image) -> {
                        if (StringUtil.isNotBlank(image)) {
                            designMKPS.add(DesignMKP.builder()
                                    .sideName(side)
                                    .images(List.of(image))
                                    .build());
                        }
                    });
                    return ItemMPK.builder()
                            .qty(String.valueOf(orderItems.size()))
                            .color(orderItem.getPrintSku().getValue1())
                            .size(orderItem.getPrintSku().getValue2())
                            .productId(orderItem.getPrintSku().getSkuCode())
                            .designs(designMKPS)
                            .build();
                })
                .collect(Collectors.toList());

        orderId = order.getId() +"-" + UUID.randomUUID().toString().split("-")[0];

        OrderData orderData = OrderData.builder()
                .city(getAddress.apply("L3"))
                .countryId("US")
                .region(StateMapper.getAbbreviation(getAddress.apply("L1")))
                .postcode(order.getRecipientAddress().getPostalCode())
                .address1(order.getRecipientAddress().getAddressLine1())
                .address2(order.getRecipientAddress().getAddressLine2())
                .firstname(order.getRecipientAddress().getFirstName())
                .lastname(order.getRecipientAddress().getLastName())
                .telephone(order.getRecipientAddress().getPhoneNumber())
                .shippingMethod(order.getPrintShippingMethod())
                .sellerOrderId(orderId)
                .prepaidLabel(order.getLabel())
                .shipmentId(order.getShippingProvider())
                .items(items)
                .build();

        return OrderRequestMKP.builder()
                .orderData(orderData)
                .build();




    }
}
