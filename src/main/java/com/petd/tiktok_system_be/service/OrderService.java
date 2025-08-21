package com.petd.tiktok_system_be.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.petd.tiktok_system_be.Specification.OrderSpecification;
import com.petd.tiktok_system_be.api.OrderApi;
import com.petd.tiktok_system_be.api.OrderDetailsApi;
import com.petd.tiktok_system_be.api.body.OrderRequestBody;
import com.petd.tiktok_system_be.dto.response.OrderResponse;
import com.petd.tiktok_system_be.entity.Order;
import com.petd.tiktok_system_be.entity.Shop;
import com.petd.tiktok_system_be.exception.AppException;
import com.petd.tiktok_system_be.repository.OrderRepository;
import com.petd.tiktok_system_be.sdk.TiktokApiResponse;
import com.petd.tiktok_system_be.sdk.appClient.RequestClient;
import com.petd.tiktok_system_be.sdk.exception.TiktokException;
import com.petd.tiktok_system_be.shared.TiktokCallApi;
import io.micrometer.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderService {

    RequestClient requestClient;
    ShopService shopService;
    OrderRepository orderRepository;

    public JsonNode getOrders (Map<String, String> params, Integer pageSize) {

        String shopId = params.get("shop_id");
        String nextPageToken = params.get("next_page_token");
        String status = StringUtils.isNotBlank(params.get("order_status")) ? params.get("order_status") : null;
        String shippingType = StringUtils.isNotBlank(params.get("shipping_type")) ? params.get("shipping_type") : null;


        Shop shop = shopService.getShopByShopId(shopId);
        String orderId = params.get("order_id");
        TiktokCallApi orderApi;
        if(StringUtils.isNotBlank(orderId)) {
            log.info("Order Id: " + orderId);
            orderApi = OrderDetailsApi.builder()
                    .requestClient(requestClient)
                    .orderId(orderId)
                    .shopCipher(shop.getCipher())
                    .accessToken(shop.getAccessToken())
                    .build();
        }else {
            OrderRequestBody orderRequestBody = OrderRequestBody.builder()
                    .orderStatus(status)
                    .shippingType(shippingType)
                    .build();

            orderApi = OrderApi.builder()
                    .requestClient(requestClient)
                    .accessToken(shop.getAccessToken())
                    .pageToken(nextPageToken)
                    .shopCipher(shop.getCipher())
                    .pageSize(pageSize)
                    .body(orderRequestBody)
                    .build();
        }
        try {
            TiktokApiResponse response = orderApi.callApi();
            return response.getData();
        }catch (TiktokException e) {
            log.error(e.getMessage());
            throw new AppException(e.getMessage(), e.getCode());
        } catch (JsonProcessingException e) {
            throw new AppException(e.getMessage(), 409);
        }
    }

    public OrderResponse getAllOrderOnDataBaseByOwnerId(
            String orderId,
            List<String> shopIds,
            String status,
            String shippingType,
            Integer page
    ) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("createTime").descending());

        Page<Order> orderPage = orderRepository.findAll(
                OrderSpecification.filterOrders(orderId, shopIds, status, shippingType),
                pageable
        );

        return OrderResponse.builder()
                .orders(orderPage.getContent())
                .totalCount(orderPage.getTotalElements())
                .currentPage(orderPage.getNumber())
                .isLast(orderPage.isLast())
                .build();
    }

}
