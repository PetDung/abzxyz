package com.petd.tiktok_system_be.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.petd.tiktok_system_be.dto.request.PrintSkuRequest;
import com.petd.tiktok_system_be.dto.request.UpdateOrderCostPrinterRequest;
import com.petd.tiktok_system_be.dto.response.ApiResponse;
import com.petd.tiktok_system_be.entity.Order.Order;
import com.petd.tiktok_system_be.service.ExportConfig.OrderExportCase;
import com.petd.tiktok_system_be.service.Order.OrderService;
import com.petd.tiktok_system_be.service.Order.OrderUpdatePrinterAndCostCase;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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
    OrderExportCase orderExportCase;

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

    @PostMapping("/change-status-print/{orderId}/{status}")
    public ApiResponse<Order> changeStatus(@PathVariable String orderId, @PathVariable String status) throws IOException {
        return ApiResponse.<Order>builder()
                .result(orderService.changeStatusPrint(orderId,status))
                .build();
    }

    @PostMapping("/update-sku-print-id/{orderItemId}")
    public ApiResponse<Order> updateSkuPrint(@PathVariable String orderItemId,
                                             @RequestBody PrintSkuRequest printSkuRequest
                                             ){
        return ApiResponse.<Order>builder()
                .result(orderService.updateSkuPrint(orderItemId,printSkuRequest))
                .build();
    }

    @PostMapping("/update-print-shipping-method/{orderId}")
    public ApiResponse<Order> updateSkuPrint(@PathVariable String orderId,
                                             @RequestParam(required = false) String method
    ){
        return ApiResponse.<Order>builder()
                .result(orderService.updatePrintShippingMethod(orderId,method))
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

    @GetMapping("/export")
    public ApiResponse<Map<String, String>> exports(@RequestParam List<String> orderIds) {
        return ApiResponse.<Map<String, String>>builder()
                .result(orderExportCase.run(orderIds))
                .build();
    }
}
