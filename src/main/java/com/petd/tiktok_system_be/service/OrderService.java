package com.petd.tiktok_system_be.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.petd.tiktok_system_be.Specification.OrderSpecification;
import com.petd.tiktok_system_be.api.OrderApi;
import com.petd.tiktok_system_be.api.OrderDetailsApi;
import com.petd.tiktok_system_be.api.body.OrderRequestBody;
import com.petd.tiktok_system_be.dto.request.UpdateOrderCostPrinter;
import com.petd.tiktok_system_be.dto.response.ResponsePage;
import com.petd.tiktok_system_be.entity.Order;
import com.petd.tiktok_system_be.entity.Printer;
import com.petd.tiktok_system_be.entity.Shop;
import com.petd.tiktok_system_be.exception.AppException;
import com.petd.tiktok_system_be.exception.ErrorCode;
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

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderService {

    RequestClient requestClient;
    ShopService shopService;
    OrderRepository orderRepository;
    PrinterService printerService;
    NotificationService notificationService;

    public Order getById(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND));
    }
    public Order save(Order order) {
        return orderRepository.save(order);
    }

    public JsonNode getOrders (String shopId, Map<String, String> params, Integer pageSize) {
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

    public Order updatePrinter (String orderId, String printerId){
        Order order = getById(orderId);
        Printer printer = "REMOVE".equals(printerId) ? null : printerService.findById(printerId);
        order.setPrinter(printer);
        orderRepository.save(order);
        notificationService.orderUpdateStatus(order);
        return order;
    }

    public Order updateCost (String orderId, BigDecimal cost){
        Order order = getById(orderId);
        order.setCost(cost);
        orderRepository.save(order);
        notificationService.orderUpdateStatus(order);
        return order;
    }

    public ResponsePage<Order> getAllOrderOnDataBaseByOwnerId(
            String orderId,
            List<String> shopIds,
            String status,
            String shippingType,
            Integer page
    ) {
        List<Shop> myShops = shopService.getMyShops();

        if(myShops == null || myShops.isEmpty()) {
            return ResponsePage.<Order>builder()
                    .orders(new ArrayList<>())
                    .totalCount(0)
                    .currentPage(0)
                    .isLast(true)
                    .build();
        }
        // Ném lỗi nếu shopIds chứa id không hợp lệ
        if(hasInvalidShopId(myShops, shopIds)){
            throw new AppException(ErrorCode.FI);
        }

        List<String> id;
        if(shopIds != null && !shopIds.isEmpty()){
            id = shopIds;
        } else {
            id = myShops.stream().map(Shop::getId).collect(Collectors.toList());
        }

        Pageable pageable = PageRequest.of(page, 10, Sort.by("createTime").descending());
        Page<Order> orderPage = orderRepository.findAll(
                OrderSpecification.filterOrders(orderId, id, status, shippingType),
                pageable
        );

        return ResponsePage.<Order>builder()
                .orders(orderPage.getContent())
                .totalCount(orderPage.getTotalElements())
                .currentPage(orderPage.getNumber())
                .isLast(orderPage.isLast())
                .build();
    }

    public boolean hasInvalidShopId(List<Shop> myShops, List<String> shopIds) {
        if(shopIds == null || shopIds.isEmpty()) return false;
        Set<String> myShopIdSet = myShops.stream()
                .map(Shop::getId)
                .collect(Collectors.toSet());
        return shopIds.stream().anyMatch(id -> !myShopIdSet.contains(id));
    }


}
