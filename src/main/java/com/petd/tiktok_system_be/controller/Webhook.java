package com.petd.tiktok_system_be.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.petd.tiktok_system_be.api.body.Event;
import com.petd.tiktok_system_be.dto.webhook.req.OrderData;
import com.petd.tiktok_system_be.dto.webhook.req.TtsNotification;
import com.petd.tiktok_system_be.service.OrderSyncService;
import com.petd.tiktok_system_be.service.WebhookService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class Webhook {

    OrderSyncService orderSyncService;
    WebhookService webhookService;

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
}
