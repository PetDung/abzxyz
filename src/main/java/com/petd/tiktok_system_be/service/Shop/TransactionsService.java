package com.petd.tiktok_system_be.service.Shop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.petd.tiktok_system_be.api.GetTransactionsByOrder;
import com.petd.tiktok_system_be.entity.Manager.Shop;
import com.petd.tiktok_system_be.exception.AppException;
import com.petd.tiktok_system_be.repository.OrderRepository;
import com.petd.tiktok_system_be.sdk.TiktokApiResponse;
import com.petd.tiktok_system_be.sdk.appClient.RequestClient;
import com.petd.tiktok_system_be.sdk.exception.TiktokException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TransactionsService {

    RequestClient requestClient;
    ShopService shopService;
    OrderRepository orderRepository;


    public JsonNode getTransactionsByOrderId(String shopId, String orderId) {
        Shop shop = shopService.getShopByShopId(shopId);
        GetTransactionsByOrder getTransactionsByOrder = GetTransactionsByOrder.builder()
                .orderId(orderId)
                .accessToken(shop.getAccessToken())
                .requestClient(requestClient)
                .shopCipher(shop.getCipher())
                .build();
        try {
            TiktokApiResponse response = getTransactionsByOrder.callApi();
            return response.getData();
        }catch (TiktokException e) {
            log.error(e.getMessage());
            throw new AppException(e.getMessage(), e.getCode());
        } catch (JsonProcessingException e) {
            throw new AppException(e.getMessage(), 409);
        }
    }
}
