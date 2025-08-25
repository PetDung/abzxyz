package com.petd.tiktok_system_be.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.petd.tiktok_system_be.api.GetWebhookApi;
import com.petd.tiktok_system_be.api.body.Event;
import com.petd.tiktok_system_be.dto.response.ApiResponse;
import com.petd.tiktok_system_be.dto.webhook.req.OrderData;
import com.petd.tiktok_system_be.dto.webhook.req.ProductData;
import com.petd.tiktok_system_be.dto.webhook.req.TtsNotification;
import com.petd.tiktok_system_be.sdk.TiktokApiResponse;
import com.petd.tiktok_system_be.sdk.appClient.RequestClient;
import com.petd.tiktok_system_be.service.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class Webhook {

    OrderSyncService orderSyncService;
    WebhookService webhookService;
    ProductService productService;
    ProductSyncService  productSyncService;

    @PostMapping("/order")
    public Boolean OrderWebhook(@RequestBody TtsNotification<OrderData> ttsNotification) throws JsonProcessingException {
        System.out.println("Nhận 1");
        orderSyncService.pushJobNotication(ttsNotification.getShopId(), ttsNotification.getData().getOrderId());
        return true;
    }

    @PostMapping("/product/change")
    public Boolean newProductWebhook(@RequestBody TtsNotification<ProductData> ttsNotification) throws JsonProcessingException {
        productSyncService.pushJob(ttsNotification);
        return true;
    }

    @PostMapping("/add")
    public boolean OrderWebhook(@RequestBody Event event) throws JsonProcessingException {
        webhookService.addWebHook(event);
        return true;
    }

    @PostMapping("/test/{shopId}/{productId}")
    public ApiResponse<JsonNode> test(
            @PathVariable String productId,
            @PathVariable String shopId
    ){
       return ApiResponse.<JsonNode>builder()
               .result(productService.getProduct(shopId, productId))
               .build();
    }

    @GetMapping("/get")
    public TiktokApiResponse get(@RequestParam("shop_id") String shopId) throws JsonProcessingException {
        return webhookService.getWebhook(shopId);
    }
}
