package com.petd.tiktok_system_be.sdk.printSdk.PrinteesHub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.petd.tiktok_system_be.entity.Order.Address;
import com.petd.tiktok_system_be.entity.Order.Order;
import com.petd.tiktok_system_be.entity.Order.OrderItem;
import com.petd.tiktok_system_be.sdk.SimpleHttpClient;
import com.petd.tiktok_system_be.sdk.printSdk.PrintSupplier;
import com.petd.tiktok_system_be.sdk.printSdk.PrinteesHub.dto.request.OrderRequest;
import com.petd.tiktok_system_be.sdk.printSdk.PrinteesHub.dto.request.ProductOption;
import com.petd.tiktok_system_be.sdk.printSdk.PrinteesHub.dto.response.OrderResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PrinteesHub implements PrintSupplier<OrderResponse> {

    String baseUrl = "https://printeeshub.com";
    String api = "/api/v1/orders/create";


    String apikey = "138afda5d7716ffb19fe32ebfe78c4f5";


    SimpleHttpClient httpClient = new SimpleHttpClient();

    @Override
    public OrderResponse print() throws IOException {

        Map<String, String> headers = Map.of(
                "X-API-Key", apikey,
                "Content-Type", "application/json"
        );
        String body = "{ \"name\": \"John\", \"age\": 30 }";

        OrderResponse response = httpClient.requestForObject(
                "https://api.example.com/orders",
                "POST",
                headers,
                body,
                OrderResponse.class
        );
        return null;
    }

    @Override
    public String buildBody(Order order) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        List<Address> addresses = order.getRecipientAddress().getDistrictInfo();


        String platformShipping = ("TIKTOK").equals(order.getShippingType()) ?  "tiktok_shipping" : "seller_shipping";
        String seller = order.getShopName();

        String country = addresses.stream()
                .filter(a -> "L0".equals(a.getAddress_level()))
                .map(Address::getAddress_name)
                .findFirst().orElse(null);

        String state = addresses.stream()
                .filter(a -> "L1".equals(a.getAddress_level()))
                .map(Address::getAddress_name)
                .findFirst().orElse(null);

        String city = addresses.stream()
                .filter(a -> "L3".equals(a.getAddress_level()))
                .map(Address::getAddress_name)
                .findFirst().orElse(null);
        String address1 = order.getRecipientAddress().getAddressLine1();
        String address2 = order.getRecipientAddress().getAddressLine2();
        String postalCode = order.getRecipientAddress().getPostalCode();
        String phone = order.getRecipientAddress().getPhoneNumber();
        String lastName = order.getRecipientAddress().getLastName();
        String firstName = order.getRecipientAddress().getFirstName();

        List<ProductOption> productOptions = new ArrayList<>();
        Map<String, List<OrderItem>> map = order.getLineItems().stream()
                .collect(Collectors.groupingBy(OrderItem::getSkuId));
        for (Map.Entry<String, List<OrderItem>> entry : map.entrySet()) {

            OrderItem orderItem = entry.getValue().get(0);

            ProductOption productOption = ProductOption.builder()
                    .sku(orderItem.getSkuName())
                    .quantity(entry.getValue().size())
                    .designFront(orderItem.getDesign().getFrontSide())
                    .designBack(orderItem.getDesign().getBackSide())
                    .mockupsFront(orderItem.getDesign().getFrontSide())
                    .mockupsBack(orderItem.getDesign().getBackSide())
                    .build();
            productOptions.add(productOption);
        }

        String urlLabel = order.getLabel();
        String trackingId = order.getTrackingNumber();
        String idOrder = order.getId();
        String node = order.getBuyerMessage();
        String methodShipping = "Standard";

        OrderRequest  orderRequest = OrderRequest.builder()
                .address1(address1)
                .address2(address2)
                .city(city)
                .country(country)
                .firstName(firstName)
                .lastName(lastName)
                .state(state)
                .phone(phone)
                .postCode(postalCode)
                .methodShipping(methodShipping)
                .platformShipping(platformShipping)
                .seller(seller)
                .productOptions(productOptions)
                .idOrder(idOrder)
                .note(node)
                .trackingId(trackingId)
                .urlLabel(urlLabel)
                .build();
        return mapper.writeValueAsString(orderRequest);
    }


}
