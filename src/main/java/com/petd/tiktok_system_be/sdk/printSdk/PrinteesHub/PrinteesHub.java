package com.petd.tiktok_system_be.sdk.printSdk.PrinteesHub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
import com.petd.tiktok_system_be.sdk.printSdk.PrinteesHub.dto.request.OrderRequest;
import com.petd.tiktok_system_be.sdk.printSdk.PrinteesHub.dto.request.ProductOption;
import com.petd.tiktok_system_be.sdk.printSdk.PrinteesHub.dto.response.Category;
import com.petd.tiktok_system_be.sdk.printSdk.PrinteesHub.dto.response.OrderResponse;
import com.petd.tiktok_system_be.sdk.printSdk.PrinteesHub.dto.response.Variation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.kafka.common.errors.ApiException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PrinteesHub implements PrintSupplier<OrderResponse> {

    String baseUrl = "https://printeeshub.com";
    String api = "/api/v1/orders/create";


    String apikey = "138afda5d7716ffb19fe32ebfe78c4f5";


    SimpleHttpClient httpClient = new SimpleHttpClient();

    @Override
    public OrderResponse print(Order order) throws IOException {
       try {
           Map<String, String> headers = Map.of(
                   "X-API-Key", "auth " + apikey,
                   "Content-Type", "application/json"
           );
           String body = buildBody(order);
           return httpClient.requestForObject(
                   baseUrl + api,
                   "POST",
                   headers,
                   body,
                   OrderResponse.class
           );
       }catch (Exception e){
           log.error(e.getMessage());
           throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
       }
    }

    public String buildBody(Order order) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        return mapper.writeValueAsString(buildBodyO(order));
    }

    public OrderRequest buildBodyO(Order order) throws JsonProcessingException {

        List<Address> addresses = order.getRecipientAddress().getDistrictInfo();

        Function<String, String> getAddress = level -> addresses.stream()
                .filter(a -> level.equals(a.getAddress_level()))
                .map(Address::getAddress_name)
                .findFirst().orElse(null);


        String platformShipping = Objects.equals(order.getShippingType(), "TIKTOK")
                ? "tiktok_shipping" : "seller_shipping";

        List<ProductOption> productOptions = order.getLineItems().stream()
                .collect(Collectors.groupingBy(OrderItem::getSkuId))
                .entrySet().stream()
                .map(e -> {
                    OrderItem item = e.getValue().get(0);
                    try {
                        return ProductOption.builder()
                                .sku(getSku(item))
                                .quantity(e.getValue().size())
                                .designFront(item.getDesign().getFrontSide())
                                .designBack(item.getDesign().getBackSide())
                                .mockupsFront(item.getDesign().getFrontSide())
                                .mockupsBack(item.getDesign().getBackSide())
                                .build();
                    } catch (IOException ex) {
                        log.warn(ex.getMessage());
                        return null;
                    }
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
                .methodShipping("Standard")
                .platformShipping(platformShipping)
                .seller(order.getShopName())
                .productOptions(productOptions)
                .idOrder(order.getId() +"-" + UUID.randomUUID().toString().split("-")[0])
                .note(order.getBuyerMessage())
                .trackingId(order.getTrackingNumber())
                .urlLabel(order.getLabel())
                .build();
    }



    public String getSku(OrderItem item) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> headers = Map.of(
                "X-API-Key", "auth " + apikey,
                "Content-Type", "application/json"
        );
        JsonNode categoriesJson = httpClient.requestForObject(
                "https://printeeshub.com/api/v1/variations",
                "GET",
                headers,
                null,
                JsonNode.class
        );

        List<Category> categories = mapper.convertValue(categoriesJson, new TypeReference<List<Category>>() {});
        List<String> keys = new ArrayList<>(Optional.ofNullable(item.getSkuPrint())
                .map(s -> Arrays.asList(s.split(",")))
                .orElse(Collections.emptyList()));

        if(keys.isEmpty()){
            String[] parts = item.getSkuName().split(",");
            String color = parts[0].trim();
            if(parts.length >= 2){
                String rest = parts[1].trim();
                String[] restParts = rest.split("\\s+");
                String size = restParts[restParts.length - 1].trim();
                String type;
                if (restParts.length == 1) {
                    type = "Classic T-Shirt";
                } else {
                    type = String.join(" ",
                            Arrays.copyOf(restParts, restParts.length - 1)
                    ).trim();
                }
                keys.add(type);
                keys.add(color);
                keys.add(size);
            }
        }
        if(keys.size() < 3){
            throw new AppException(ErrorCode.RQ);
        }

        log.info(keys.get(0), keys.get(1),  keys.get(2));

        List<Variation> variations = categories.stream()
                .filter(c -> c.getName().equalsIgnoreCase(keys.get(0)))
                .flatMap(c -> c.getVariations().stream())
                .toList();
        log.info(String.valueOf(variations.size()));


        Variation variation = variations.stream()
                .filter(v -> fuzzyMatch(v.getColor(), keys.get(1), 2)   // so khớp gần đúng cho color
                        && v.getSize().equalsIgnoreCase(keys.get(2)))   // so khớp size chính xác
                .findFirst()
                .orElse(null);

        if(variation == null){
            throw new AppException(ErrorCode.NOT_FOUND);
        }
        return variation.getSku();
    }

    public static boolean fuzzyMatch(String s1, String s2, int threshold) {
        LevenshteinDistance distance = new LevenshteinDistance();
        int score = distance.apply(s1.toLowerCase(), s2.toLowerCase());
        return score <= threshold; // threshold = số ký tự cho phép sai
    }


}