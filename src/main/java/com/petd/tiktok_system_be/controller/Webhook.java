package com.petd.tiktok_system_be.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.petd.tiktok_system_be.api.body.Event;
import com.petd.tiktok_system_be.dto.response.ApiResponse;
import com.petd.tiktok_system_be.dto.webhook.req.OrderData;
import com.petd.tiktok_system_be.dto.webhook.req.TtsNotification;
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
    public ResponseEntity<byte[]> test(@RequestParam String url) {
        try (InputStream inputStream = googleDriveService.getPdfFromUrl(url)) {
            byte[] pdfBytes = inputStream.readAllBytes();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"file.pdf\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
