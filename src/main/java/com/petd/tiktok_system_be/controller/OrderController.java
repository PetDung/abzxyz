package com.petd.tiktok_system_be.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.petd.tiktok_system_be.dto.response.ApiResponse;
import com.petd.tiktok_system_be.service.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderController {

    OrderService orderService;

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
}
