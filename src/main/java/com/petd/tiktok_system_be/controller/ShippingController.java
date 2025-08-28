package com.petd.tiktok_system_be.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.petd.tiktok_system_be.dto.response.ApiResponse;
import com.petd.tiktok_system_be.service.ShippingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shipping")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ShippingController {

    ShippingService shippingService;


    @GetMapping("/label")
    public ApiResponse<?> get (
            @RequestParam(name = "order_id") String orderId,
            @RequestParam(name = "shop_id") String shopId
    ) throws JsonProcessingException {
        JsonNode r = shippingService.getLabel(orderId, shopId);
        return ApiResponse.<JsonNode>builder()
                .result(r)
                .build();
    }

    @GetMapping("/label/buy")
    public ApiResponse<?> buyLabel (
            @RequestParam(name = "order_id") String orderId,
            @RequestParam(name = "shop_id") String shopId
    ) throws JsonProcessingException {
        JsonNode r = shippingService.buyLabel(orderId, shopId);
        return ApiResponse.<JsonNode>builder()
                .result(r)
                .build();
    }
}
