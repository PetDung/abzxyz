package com.petd.tiktok_system_be.sdk.printSdk.mango;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.petd.tiktok_system_be.entity.Design.Design;
import com.petd.tiktok_system_be.entity.Order.Address;
import com.petd.tiktok_system_be.entity.Order.Order;
import com.petd.tiktok_system_be.entity.Order.OrderItem;
import com.petd.tiktok_system_be.exception.AppException;
import com.petd.tiktok_system_be.sdk.SimpleHttpClient;
import com.petd.tiktok_system_be.sdk.printSdk.MenPrint.dto.response.Response;
import com.petd.tiktok_system_be.sdk.printSdk.PrintSupplier;
import com.petd.tiktok_system_be.sdk.printSdk.PrinteesHub.dto.response.OrderResponse;
import com.petd.tiktok_system_be.sdk.printSdk.mango.dto.request.FileInfo;
import com.petd.tiktok_system_be.sdk.printSdk.mango.dto.request.Item;
import com.petd.tiktok_system_be.sdk.printSdk.mango.dto.request.OrderRequest;
import io.micrometer.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class MangoPrint implements PrintSupplier {

    SimpleHttpClient httpClient = new SimpleHttpClient(60);
    ObjectMapper objectMapper = new ObjectMapper();

    String apikey = "CWyUfOTKdqnxyISd9OGQdy8wOpeQbuaJ";
    String baseUrl = "https://v3.mangoteeprints.com";
    Map<String, String> headers = Map.of(
            "X-API-Key", apikey,
            "Accept", "application/json"
    );

    @Override
    public OrderResponse print(Order order) throws IOException {
        String api = "/api/public/v1/orders";
        OrderRequest body = buildBodyO(order);
        String bodyJson = objectMapper.writeValueAsString(body);
        httpClient.requestForObject(
                baseUrl + api,
                "POST",
                headers,
                bodyJson
        );
        order.setOrderFulfillId(body.getOrder_id());
        return synchronize(order);
    }

    @Override
    public String getCode() {
        return "MG";
    }

    @Override
    public OrderResponse cancel(Order order) throws IOException {
        String api = "/api/public/v1/orders/" +  order.getOrderFulfillId();
        httpClient.requestForObject(
                baseUrl + api,
                "DELETE",
                headers,
                ""
        );
        return new OrderResponse();
    }

    @Override
    public OrderResponse synchronize(Order order) throws IOException {

       try{
           JsonNode rootOrderResponse = getPrintOrderById(order.getOrderFulfillId());

           boolean status = rootOrderResponse.get("status").asBoolean();
           if(!status) return new OrderResponse();

           JsonNode data = rootOrderResponse.get("data");
           BigDecimal bigDecimal =  new BigDecimal(data.get("total").asText());
           String orderStatus = data.get("status").asText();

           return OrderResponse.builder()
                   .orderId(order.getOrderFulfillId())
                   .amount(bigDecimal)
                   .orderFulfillId(order.getOrderFulfillId())
                   .originPrintStatus(orderStatus)
                   .build();
       }catch (IOException e) {
           log.error(e.getMessage());
           Response response = objectMapper.readValue(e.getMessage(), Response.class);
           throw new AppException(409, response.getMsg());
       }
    }

    @Override
    public JsonNode getPrintOrderById(String orderId) throws IOException {

        String api = "/api/public/v1/orders/" +  orderId;

        String response = httpClient.requestForObject(
                baseUrl + api,
                "get",
                headers,
                ""
        );
        log.info(response);
        return objectMapper.readTree(response);
    }

    public String buildBodyJson(Order order) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return mapper.writeValueAsString(buildBodyO(order));
    }


    public OrderRequest buildBodyO(Order order) {

        List<Address> addresses = order.getRecipientAddress().getDistrictInfo();

        Function<String, String> getAddress = level -> addresses.stream()
                .filter(a -> level.equals(a.getAddress_level()))
                .map(Address::getAddress_name)
                .findFirst().orElse(null);

        List<Item> items = order.getLineItems().stream()
                .filter(orderItem -> orderItem.getIsPrint() == true)
                .collect(Collectors.groupingBy(OrderItem::getSkuId))
                .values().stream()
                .map(orderItems -> {
                    OrderItem item = orderItems.get(0);

                    Design design = item.getDesign();
                    List<FileInfo> print_files = new ArrayList<>();
                    List<FileInfo> preview_files = new ArrayList<>();

                    if (StringUtils.isNotBlank(item.getDesign().getFrontSide())) {
                       print_files.add(FileInfo.builder()
                                       .key("front")
                                       .url(item.getDesign().getFrontSide())
                                        .build());
                        preview_files.add(FileInfo.builder()
                                .key("front")
                                .url(item.getSkuImage())
                                .build());
                    }

                    if (StringUtils.isNotBlank(item.getDesign().getBackSide())) {
                        print_files.add(FileInfo.builder()
                                .key("back")
                                .url(item.getDesign().getBackSide())
                                .build());
                        preview_files.add(FileInfo.builder()
                                .key("back")
                                .url(item.getSkuImage())
                                .build());
                    }

                    if (StringUtils.isNotBlank(item.getDesign().getLeftSide())) {
                        print_files.add(FileInfo.builder()
                                .key("right_sleeve")
                                .url(item.getDesign().getLeftSide())
                                .build());
                        preview_files.add(FileInfo.builder()
                                .key("right_sleeve")
                                .url(item.getSkuImage())
                                .build());
                    }

                    if (StringUtils.isNotBlank(item.getDesign().getRightSide())) {
                        print_files.add(FileInfo.builder()
                                .key("left_sleeve")
                                .url(item.getDesign().getRightSide())
                                .build());
                        preview_files.add(FileInfo.builder()
                                .key("left_sleeve")
                                .url(item.getSkuImage())
                                .build());
                    }
                    return Item.builder()
                            .sku(item.getPrintSku().getSkuCode())
                            .quantity(orderItems.size())
                            .print_files(print_files)
                            .preview_files(preview_files)
                            .build();
                })
                .collect(Collectors.toList());

        return OrderRequest.builder()
                .label_url(order.getLabel())
                .order_id(order.getId() +"-" + UUID.randomUUID().toString().split("-")[0])
                .seller(order.getShopName())
                .shipping_method(order.getPrintShippingMethod())
                .first_name(order.getRecipientAddress().getFirstName())
                .last_name(order.getRecipientAddress().getLastName())
                .address_line_1(order.getRecipientAddress().getAddressLine1())
                .address_line_2(order.getRecipientAddress().getAddressLine2())
                .city(getAddress.apply("L3"))
                .state(getAddress.apply("L1"))
                .country("US")
                .zip(order.getRecipientAddress().getPostalCode())
                .phone(order.getRecipientAddress().getPhoneNumber())
                .items(items)
                .build();
    }

}
