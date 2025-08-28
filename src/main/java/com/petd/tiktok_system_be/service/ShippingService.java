package com.petd.tiktok_system_be.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.petd.tiktok_system_be.api.CreatePackages;
import com.petd.tiktok_system_be.api.GetEligibleShippingService;
import com.petd.tiktok_system_be.api.GetPackageShippingDocument;
import com.petd.tiktok_system_be.api.body.CreatePackagesBody;
import com.petd.tiktok_system_be.dto.message.LabelMessage;
import com.petd.tiktok_system_be.entity.Order;
import com.petd.tiktok_system_be.entity.Shop;
import com.petd.tiktok_system_be.exception.AppException;
import com.petd.tiktok_system_be.exception.ErrorCode;
import com.petd.tiktok_system_be.sdk.TiktokApiResponse;
import com.petd.tiktok_system_be.sdk.appClient.RequestClient;
import com.petd.tiktok_system_be.sdk.exception.TiktokException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ShippingService {

    RequestClient requestClient;
    ShopService shopService;
    OrderService orderService;
    KafkaTemplate<String, String> kafkaTemplate;
    ObjectMapper objectMapper;


    public JsonNode getShipping (String orderId, String shopId){

        Shop shop = shopService.getShopByShopId(shopId);

        GetEligibleShippingService getEligibleShippingService = GetEligibleShippingService.builder()
                .orderId(orderId)
                .accessToken(shop.getAccessToken())
                .requestClient(requestClient)
                .shopCipher(shop.getCipher())
                .body(null)
                .build();
        try{
            TiktokApiResponse response = getEligibleShippingService.callApi();
            return response.getData();
        }catch (TiktokException e) {
            log.error( "{} : {}",orderId + "-" + shopId,e.getMessage());
            throw new AppException(e.getMessage(), e.getCode());
        } catch (JsonProcessingException e) {
            throw new AppException(e.getMessage(), 409);
        }
    }


    public JsonNode buyLabel (String orderId, String shopId){

        Shop shop = shopService.getShopByShopId(shopId);

        CreatePackages createPackages = CreatePackages.builder()
                .accessToken(shop.getAccessToken())
                .requestClient(requestClient)
                .shopCipher(shop.getCipher())
                .body(CreatePackagesBody.builder()
                        .shippingServiceId("7208502187360519982")
                        .orderId(orderId)
                        .build()
                )
                .build();
        try{
            TiktokApiResponse response = createPackages.callApi();
            return response.getData();
        }catch (TiktokException e) {
            log.error(e.getMessage());
            throw new AppException(e.getMessage(), e.getCode());
        } catch (JsonProcessingException e) {
            throw new AppException(e.getMessage(), 409);
        }
    }

    public JsonNode getLabel (String orderId, String shopId) throws JsonProcessingException {

        Shop shop = shopService.getShopByShopId(shopId);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        Map<String, String> params = new HashMap<>();
        params.put("next_page_token","");
        params.put("order_id", orderId);

        JsonNode jsonNode = orderService.getOrders(shopId, params, 10);
        JsonNode ordersNode = jsonNode.get("orders");

        if (ordersNode == null || !ordersNode.isArray() || ordersNode.isEmpty()) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        String packageId = null;

        for (JsonNode node : ordersNode) {
            JsonNode packagesNode = node.get("packages");
            if (packagesNode != null && packagesNode.isArray() && !packagesNode.isEmpty()) {
                packageId = packagesNode.get(0).get("id").asText();
                break;
            }
        }

        if (packageId == null) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        GetPackageShippingDocument getPackageShippingDocument = GetPackageShippingDocument.builder()
                .accessToken(shop.getAccessToken())
                .packageId(packageId)
                .shopCipher(shop.getCipher())
                .requestClient(requestClient)
                .build();
        try{
            TiktokApiResponse response = getPackageShippingDocument.callApi();
            if(response.getCode() == 0 ){
                String labelUrl = response.getData().get("doc_url").asText();
                String trackingNumber = response.getData().get("tracking_number").asText();

                LabelMessage message = LabelMessage.builder()
                        .orderId(orderId)
                        .label(labelUrl)
                        .trackingNumber(trackingNumber)
                        .build();
                kafkaTemplate.send("order-get-label", objectMapper.writeValueAsString(message));
            }
            return response.getData();
        }catch (TiktokException e) {
            log.error(e.getMessage());
            throw new AppException(e.getMessage(), e.getCode());
        }
    }
}
