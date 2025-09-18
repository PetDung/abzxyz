package com.petd.tiktok_system_be.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.petd.tiktok_system_be.dto.request.UpdateOrderCostPrinterRequest;
import com.petd.tiktok_system_be.dto.response.ApiResponse;
import com.petd.tiktok_system_be.entity.Order.Order;
import com.petd.tiktok_system_be.service.Order.OrderService;
import com.petd.tiktok_system_be.service.Order.OrderUpdatePrinterAndCostCase;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderController {

    OrderService orderService;
    OrderUpdatePrinterAndCostCase updatePrinterAndCostCase;

    @PreAuthorize("@shopSecurity.isAccept(#shopId)")
    @PostMapping("/list/{shopId}")
    public ApiResponse<JsonNode> getOrder(
            @RequestParam Map<String, String> params,
            @PathVariable String shopId
    ){
        return ApiResponse.<JsonNode>builder()
                .result(orderService.getOrders(shopId,params, 10))
                .build();
    }

    @PostMapping("/printer/{orderId}/{printerId}")
    public ApiResponse<Order> updatePrinter(@PathVariable String orderId, @PathVariable String printerId){
        return ApiResponse.<Order>builder()
                .result(orderService.updatePrinter(orderId,printerId))
                .build();
    }

    @PostMapping("/cost/{orderId}")
    public ApiResponse<Order> updateCost(
            @PathVariable String orderId,
            @RequestParam BigDecimal cost){
        return ApiResponse.<Order>builder()
                .result(orderService.updateCost(orderId,cost))
                .build();
    }

    @PostMapping("/update-in-file")
    public ApiResponse<Map<Integer, List<String>>> updateInFile(@RequestBody UpdateOrderCostPrinterRequest request){
        return ApiResponse.<Map<Integer, List<String>>>builder()
                .result(updatePrinterAndCostCase.updatePrinterAndCost(request.getData()))
                .build();
    }
}
