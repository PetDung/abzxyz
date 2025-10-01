package com.petd.tiktok_system_be.sdk.printSdk.MenPrint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.petd.tiktok_system_be.entity.Order.Address;
import com.petd.tiktok_system_be.entity.Order.Order;
import com.petd.tiktok_system_be.entity.Order.OrderItem;
import com.petd.tiktok_system_be.exception.AppException;
import com.petd.tiktok_system_be.exception.ErrorCode;
import com.petd.tiktok_system_be.sdk.SimpleHttpClient;
import com.petd.tiktok_system_be.sdk.printSdk.MenPrint.dto.request.OrderItemPrintRequest;
import com.petd.tiktok_system_be.sdk.printSdk.MenPrint.dto.request.OrderRequest;
import com.petd.tiktok_system_be.sdk.printSdk.MenPrint.dto.response.Response;
import com.petd.tiktok_system_be.sdk.printSdk.PrintSupplier;
import com.petd.tiktok_system_be.sdk.printSdk.PrinteesHub.dto.response.OrderResponse;
import io.micrometer.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class MenPrint implements PrintSupplier {
    SimpleHttpClient httpClient = new SimpleHttpClient(60);
    ObjectMapper objectMapper = new ObjectMapper();

    String apikey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJuZ3V5ZW5raGFuZzEwMDAxOTl4QGdtYWlsLmNvbSIsInNpdGVfaWQiOiJNZW5wcmludCIsImV4cCI6MTc5MDY2NjM3N30.JZuHbcrJKGtGRMNgGlJ5KVftTyfM-HOt1gKFScVVNhg";
    String baseUrl = "https://pubapi.menprint.com";
    Map<String, String> headers = Map.of(
            "Authorization", apikey,
            "Accept", "application/json"
    );
    @Override
    public OrderResponse print(Order order) throws IOException {
      try{
          String api = "/api/v3/orders/single_order";
          String bodyJson = buildBodyJson(order);
          String response = httpClient.requestForObject(
                  baseUrl + api,
                  "POST",
                  headers,
                  bodyJson
          );
          JsonNode root = objectMapper.readTree(response);
          JsonNode dataArray = root.get("data");
          String orderId = "";
          if (dataArray != null && dataArray.isArray() && !dataArray.isEmpty()) {
              orderId = dataArray.get(0).asText();
          }
          JsonNode rootOrderResponse = getPrintOrderById(orderId);
          BigDecimal bigDecimal =  new BigDecimal(rootOrderResponse.get("total").asText());
          return OrderResponse.builder()
                  .orderId(orderId)
                  .amount(bigDecimal)
                  .orderFulfillId(orderId)
                  .build();
      }catch (IOException e){
          log.error(e.getMessage());
          Response response = objectMapper.readValue(e.getMessage(), Response.class);
          throw new AppException(409, response.getMsg());
      }catch (Exception e){
          log.error(e.getMessage());
          throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
      }
    }
    @Override
    public String getCode() {
        return "MP";
    }

    @Override
    public OrderResponse cancel(Order order) throws IOException {
       try{
           String api = "/api/v3/orders/" + order.getOrderFulfillId()  + "/delete";
           Response response = httpClient.requestForObject(
                   baseUrl + api,
                   "DELETE",
                   headers,
                   "",
                   Response.class
           );
           return new OrderResponse();
       }catch (IOException e){
           log.error(e.getMessage());
           Response response = objectMapper.readValue(e.getMessage(), Response.class);

           String message = response.getMsg();
           if(StringUtils.isNotBlank(message) && message.contains("Order not found")){
               return new OrderResponse();
           }
           throw new AppException(409, message);
       }catch (Exception e){
           log.error(e.getMessage());
           throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
       }
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

        List<OrderItemPrintRequest> items = order.getLineItems().stream()
                .filter(orderItem -> orderItem.getIsPrint() == true)
                .collect(Collectors.groupingBy(OrderItem::getSkuId))
                .values().stream()
                .map(orderItems -> {
                    OrderItem item = orderItems.get(0);
                    OrderItemPrintRequest itemResponse = OrderItemPrintRequest.builder()
                            .sku(item.getPrintSku().getSkuCode())
                            .quantity(orderItems.size())
                            .frontUrl(item.getDesign().getFrontSide())
                            .mockupFrontUrl(item.getSkuImage())
                            .backUrl(item.getDesign().getBackSide())
                            .leftSleeve(item.getDesign().getLeftSide())
                            .rightSleeve(item.getDesign().getRightSide())
                            .note(order.getBuyerMessage())
                            .build();
                    if(StringUtils.isBlank(itemResponse.getFrontUrl())){
                        itemResponse.setPrintSizeFront(null);
                    }
                    if(StringUtils.isBlank(itemResponse.getBackUrl())){
                        itemResponse.setPrintSizeBack(null);
                    }

                    return itemResponse;
                })
                .collect(Collectors.toList());

        return OrderRequest.builder()
                .labelLink(order.getLabel())
                .orderId(order.getId() +"-" + UUID.randomUUID().toString().split("-")[0])
                .seller(order.getShopName())
                .shippingMethod(order.getPrintShippingMethod())
                .orderSource(order.getId())
                .firstName(order.getRecipientAddress().getFirstName())
                .lastName(order.getRecipientAddress().getLastName())
                .addressLine1(order.getRecipientAddress().getAddressLine1())
                .addressLine2(order.getRecipientAddress().getAddressLine2())
                .city(getAddress.apply("L3"))
                .stateOrRegion(getAddress.apply("L1"))
                .countryCode(getAddress.apply("L0"))
                .zip(order.getRecipientAddress().getPostalCode())
                .phone(order.getRecipientAddress().getPhoneNumber())
                .items(items)
                .build();
    }


    public JsonNode getPrintOrderById (String orderId) throws IOException {
        String api = "/api/v3/orders/" + orderId;
        String response = httpClient.requestForObject(
                baseUrl + api,
                "get",
                headers,
                ""
        );
        return objectMapper.readTree(response);
    }
}
