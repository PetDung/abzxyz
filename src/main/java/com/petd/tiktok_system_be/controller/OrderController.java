package com.petd.tiktok_system_be.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.petd.tiktok_system_be.dto.response.ApiResponse;
import com.petd.tiktok_system_be.service.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderController {

    OrderService orderService;


    @PostMapping("/list")
    public ApiResponse<JsonNode> getOrder(@RequestParam Map<String, String> params){

        String shopId = params.get("shop_id");
        String nextPageToken = params.get("next_page_token");
        return ApiResponse.<JsonNode>builder()
                .result(orderService.getOrders(shopId, nextPageToken))
                .build();
    }
}
