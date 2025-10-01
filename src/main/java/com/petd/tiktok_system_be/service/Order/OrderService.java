package com.petd.tiktok_system_be.service.Order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.petd.tiktok_system_be.Specification.OrderSpecification;
import com.petd.tiktok_system_be.api.OrderApi;
import com.petd.tiktok_system_be.api.OrderDetailsApi;
import com.petd.tiktok_system_be.api.body.OrderRequestBody;
import com.petd.tiktok_system_be.constant.PrintStatus;
import com.petd.tiktok_system_be.dto.request.PrintSkuRequest;
import com.petd.tiktok_system_be.dto.response.ResponsePage;
import com.petd.tiktok_system_be.entity.Order.Order;
import com.petd.tiktok_system_be.entity.Manager.Printer;
import com.petd.tiktok_system_be.entity.Manager.Shop;
import com.petd.tiktok_system_be.entity.Order.OrderItem;
import com.petd.tiktok_system_be.entity.Order.PrintSku;
import com.petd.tiktok_system_be.exception.AppException;
import com.petd.tiktok_system_be.exception.ErrorCode;
import com.petd.tiktok_system_be.repository.OrderItemRepository;
import com.petd.tiktok_system_be.repository.OrderRepository;
import com.petd.tiktok_system_be.repository.PrintSkuRepository;
import com.petd.tiktok_system_be.sdk.TiktokApiResponse;
import com.petd.tiktok_system_be.sdk.appClient.RequestClient;
import com.petd.tiktok_system_be.sdk.exception.TiktokException;
import com.petd.tiktok_system_be.service.Lib.NotificationService;
import com.petd.tiktok_system_be.service.Manager.PrinterService;
import com.petd.tiktok_system_be.service.PrintCase.HandlePrintOrderCase;
import com.petd.tiktok_system_be.service.Shop.ShopService;
import com.petd.tiktok_system_be.shared.TiktokCallApi;
import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
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
    OrderItemRepository orderItemRepository;
    PrintSkuRepository printSkuRepository;
    HandlePrintOrderCase handlePrintOrderCase;

    public Order getById(String id) {
        return orderRepository.findById(id)
                 .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND));
    }
    @Transactional
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

            if(StringUtils.isNotBlank(params.get("create_time_ge"))) {
                Long createTimeGe = Long.parseLong(params.get("create_time_ge"));
                Long createTimeLt = Long.parseLong(params.get("create_time_lt"));
                orderRequestBody.setCreateTimeGe(createTimeGe);
                orderRequestBody.setCreateTimeLt(createTimeLt);
            }

            orderApi = OrderApi.builder()
                    .requestClient(requestClient)
                    .accessToken(shop.getAccessToken())
                    .pageToken(nextPageToken)
                    .shopCipher(shop.getCipher())
                    .pageSize(pageSize)
                    .body(orderRequestBody)
                    .build();
        }
        try{
            TiktokApiResponse response = orderApi.callApi();
            return response.getData();
        }catch (TiktokException e) {
            log.error(e.getMessage());
            throw new AppException(e.getMessage(), e.getCode());
        } catch (JsonProcessingException e) {
            throw new AppException(e.getMessage(), 409);
        }
    }

    @Transactional
    public Order updatePrinter (String orderId, String printerId){
        Order order = getById(orderId);
        Printer printer = "REMOVE".equals(printerId) ? null : printerService.findById(printerId);
        order.setPrinter(printer);
        order.setPrintShippingMethod(null);
        order.getLineItems().forEach(lineItem -> {
            lineItem.setPrintSku(null);
        });
        orderRepository.save(order);
        notificationService.orderUpdateStatus(order);
        return order;
    }

    @Transactional
    public Order updateIsPrint(List<String> lineItemIds, Boolean isPrint) {
        List<OrderItem> orderItem = orderItemRepository.findAllById(lineItemIds);
        orderItem.forEach(item ->{
            Order order = item.getOrder();
            item.setIsPrint(isPrint);
            notificationService.orderUpdateStatus(order);
        });
        orderItemRepository.saveAll(orderItem);
        Order order = orderItem.get(0).getOrder();
        notificationService.orderUpdateStatus(order);
        return orderItem.get(0).getOrder();
    }


    @Transactional
    public Order updateSkuPrint(List<String> lineItemIds, PrintSkuRequest printSkuRequest) {
        validatePrintSkuRequest(printSkuRequest);

        List<OrderItem> orderItem = orderItemRepository.findAllById(lineItemIds);
        orderItem.forEach(item ->{
            Order order = item.getOrder();
            if (order.getPrinter() == null) {
                throw new AppException(ErrorCode.RQ);
            }
            PrintSku printSku = printSkuRepository
                    .findByPrintCodeAndSkuCode(order.getPrinter().getId(), printSkuRequest.getSkuCode())
                    .orElseGet(() -> createAndSavePrintSku(order.getPrinter().getId(), printSkuRequest));
            item.setPrintSku(printSku);
        });
        orderItemRepository.saveAll(orderItem);
        Order order = orderItem.get(0).getOrder();
        notificationService.orderUpdateStatus(order);
        return order;
    }


    @Transactional
    public Order updatePrintShippingMethod(String orderId, String shippingMethodId) {
        Order order = getById(orderId);
        order.setPrintShippingMethod(shippingMethodId);
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

        List<String> statuses = StringUtils.isNotBlank(status) ? List.of(status) :  List.of() ;

        List<Shop> myShops = shopService.getMyShops();

        if(myShops == null || myShops.isEmpty()) {
            return ResponsePage.<Order>builder()
                    .data(new ArrayList<>())
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

        Pageable pageable = PageRequest.of(page, 20, Sort.by("createTime").descending());
        Page<Order> orderPage = orderRepository.findAll(
                OrderSpecification.filterOrders(orderId, id, statuses, shippingType),
                pageable
        );

        return ResponsePage.<Order>builder()
                .data(orderPage.getContent())
                .totalCount(orderPage.getTotalElements())
                .currentPage(orderPage.getNumber())
                .isLast(orderPage.isLast())
                .build();
    }

    public Order changeStatusPrint(String orderId, String status) throws IOException {
        List<String> codes = Arrays.stream(PrintStatus.values())
                .map(Enum::name)
                .toList();
        if (!codes.contains(status.toUpperCase())) {
            throw new AppException(ErrorCode.NOT_FOUND);
        }
        Order order = getById(orderId);
        if(PrintStatus.PRINT_CANCEL.toString().equals(status)){
            order = handlePrintOrderCase.cancel(order);
        }
        order.setPrintStatus(status);
        if(PrintStatus.PRINT_REQUEST.toString().equals(status)){
            order = handlePrintOrderCase.printOrder(order);
        }
        orderRepository.save(order);
        notificationService.orderUpdateStatus(order);
        return order;
    }
    public boolean hasInvalidShopId(List<Shop> myShops, List<String> shopIds) {
        if(shopIds == null || shopIds.isEmpty()) return false;
        Set<String> myShopIdSet = myShops.stream()
                .map(Shop::getId)
                .collect(Collectors.toSet());
        return shopIds.stream().anyMatch(id -> !myShopIdSet.contains(id));
    }

    private PrintSku createAndSavePrintSku(String printerId, PrintSkuRequest request) {
        PrintSku newPrintSku = PrintSku.builder()
                .skuCode(request.getSkuCode())
                .type(request.getType())
                .value1(request.getValue1())
                .value2(request.getValue2())
                .printCode(printerId)
                .build();
        return printSkuRepository.save(newPrintSku);
    }

    private void validatePrintSkuRequest(PrintSkuRequest request) {
        if (request.getSkuCode() == null || request.getSkuCode().isBlank()) {
            throw new AppException(ErrorCode.RQ);
        }
        if (request.getType() == null) {
            throw new AppException(ErrorCode.RQ);
        }
        // validate value1, value2 nếu cần
        if (request.getValue1() == null || request.getValue1().isBlank()) {
            throw new AppException(ErrorCode.RQ);
        }
    }


}
