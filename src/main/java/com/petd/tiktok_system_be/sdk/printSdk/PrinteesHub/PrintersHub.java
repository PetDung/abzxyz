package com.petd.tiktok_system_be.sdk.printSdk.PrinteesHub;

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
import com.petd.tiktok_system_be.sdk.printSdk.PrintSupplier;
import com.petd.tiktok_system_be.sdk.printSdk.PrinteesHub.dto.exception.PrintersHubApiException;
import com.petd.tiktok_system_be.sdk.printSdk.PrinteesHub.dto.request.OrderRequest;
import com.petd.tiktok_system_be.sdk.printSdk.PrinteesHub.dto.request.ProductOption;
import com.petd.tiktok_system_be.sdk.printSdk.PrinteesHub.dto.response.OrderResponse;
import io.micrometer.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PrintersHub implements PrintSupplier {

    SimpleHttpClient httpClient = new SimpleHttpClient();
    ObjectMapper objectMapper = new ObjectMapper();

    String apikey = "138afda5d7716ffb19fe32ebfe78c4f5";
    String baseUrl = "https://printeeshub.com";
    Map<String, String> headers = Map.of(
            "X-API-Key", "auth " + apikey,
            "Content-Type", "application/json"
    );

    @Override
    public OrderResponse print(Order order) throws JsonProcessingException {
       try {

           String body = buildBodyJson(order);
           String api = "/api/v1/orders/create";
           return httpClient.requestForObject(
                   baseUrl + api,
                   "POST",
                   headers,
                   body,
                   OrderResponse.class
           );
       }catch (IOException e){
           PrintersHubApiException printersHubApiException = objectMapper.readValue(e.getMessage(), PrintersHubApiException.class);
           throw new AppException(409, printersHubApiException.getMessage());
       }catch (Exception e){
           log.error(e.getMessage());
           throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
       }
    }

    @Override
    public String getCode() {
        return "PRH";
    }

    @Override
    public OrderResponse cancel(Order order) throws IOException {
      try{
          String api = "/api/v1/orders/" + order.getOrderFulfillId() +"/cancel";
          return httpClient.requestForObject(
                  baseUrl + api,
                  "PUT",
                  headers,
                  "",
                  OrderResponse.class
          );
      }catch (IOException e){
          log.error(e.getMessage());
          PrintersHubApiException printersHubApiException = objectMapper.readValue(e.getMessage(), PrintersHubApiException.class);
          String message = printersHubApiException.getMessage();
          if(StringUtils.isNotBlank(message) && message.contains("The order cannot be canceled. Order is CANCEL")){
              return null;
          }
          throw new AppException(409, printersHubApiException.getMessage());
      }catch (Exception e){
          log.error(e.getMessage());
          throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
      }
    }

    @Override
    public OrderResponse synchronize(Order order) throws IOException {
        JsonNode rootOrderResponse = getPrintOrderById(order.getOrderFulfillId());
        BigDecimal bigDecimal =  new BigDecimal(rootOrderResponse.get("amount").asText());
        String status = rootOrderResponse.get("status").asText();
        return OrderResponse.builder()
                .amount(bigDecimal)
                .orderFulfillId(order.getOrderFulfillId())
                .originPrintStatus(status)
                .build();
    }
    @Override
    public JsonNode getPrintOrderById(String orderId) throws IOException {
        String api = "/api/v1/orders/" +  orderId;

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


        String platformShipping = Objects.equals(order.getShippingType(), "TIKTOK")
                ? "tiktok_shipping" : "seller_shipping";

        List<ProductOption> productOptions = order.getLineItems().stream()
                .filter(orderItem -> orderItem.getIsPrint() == true)
                .collect(Collectors.groupingBy(OrderItem::getSkuId))
                .values().stream()
                .map(orderItems -> {
                    OrderItem item = orderItems.get(0);
                    return ProductOption.builder()
                            .sku(item.getPrintSku().getSkuCode())
                            .quantity(orderItems.size())
                            .designFront(item.getDesign().getFrontSide())
                            .designBack(item.getDesign().getBackSide())
                            .mockupsFront(item.getSkuImage())
                            .mockupsBack(item.getSkuImage())
                            .build();
                })
                .collect(Collectors.toList());

        return OrderRequest.builder()
                .address1(order.getRecipientAddress().getAddressLine1())
                .address2(order.getRecipientAddress().getAddressLine2())
                .city(getAddress.apply("L3"))
                .country(getAddress.apply("L0"))
                .state(getAddress.apply("L1"))
                .firstName(order.getRecipientAddress().getFirstName())
                .lastName(order.getRecipientAddress().getLastName())
                .phone(order.getRecipientAddress().getPhoneNumber())
                .postCode(order.getRecipientAddress().getPostalCode())
                .methodShipping(order.getPrintShippingMethod())
                .platformShipping(platformShipping)
                .seller(order.getShopName())
                .productOptions(productOptions)
                .idOrder(order.getId() +"-" + UUID.randomUUID().toString().split("-")[0])
                .note(order.getBuyerMessage())
                .trackingId(order.getTrackingNumber())
                .urlLabel(order.getLabel())
                .build();
    }
}