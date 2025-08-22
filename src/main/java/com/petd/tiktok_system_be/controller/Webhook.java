package com.petd.tiktok_system_be.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.petd.tiktok_system_be.api.GetWebhookApi;
import com.petd.tiktok_system_be.api.body.Event;
import com.petd.tiktok_system_be.dto.response.ApiResponse;
import com.petd.tiktok_system_be.dto.webhook.req.OrderData;
import com.petd.tiktok_system_be.dto.webhook.req.TtsNotification;
import com.petd.tiktok_system_be.sdk.TiktokApiResponse;
import com.petd.tiktok_system_be.sdk.appClient.RequestClient;
import com.petd.tiktok_system_be.service.GoogleDriveService;
import com.petd.tiktok_system_be.service.OrderSyncService;
import com.petd.tiktok_system_be.service.ShippingService;
import com.petd.tiktok_system_be.service.WebhookService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class Webhook {

    OrderSyncService orderSyncService;
    WebhookService webhookService;
    ShippingService shippingService;
    GoogleDriveService googleDriveService;
    RequestClient requestClient;

    @PostMapping("/order")
    public Boolean OrderWebhook(@RequestBody TtsNotification<OrderData> ttsNotification) throws JsonProcessingException {
        orderSyncService.pushJobNotication(ttsNotification.getShopId(), ttsNotification.getData().getOrderId());
        return true;
    }

    @PostMapping("/add")
    public boolean OrderWebhook(@RequestBody Event event) throws JsonProcessingException {
        webhookService.addWebHook(event);
        return true;
    }

    @PostMapping("/test")
    public boolean test() throws JsonProcessingException {
       orderSyncService.pushJob();
       return true;
    }

    @GetMapping("/get")
    public TiktokApiResponse get(@RequestParam("shop_id") String shopId) throws JsonProcessingException {
        return webhookService.getWebhook(shopId);
    }
}
