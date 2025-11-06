package com.petd.tiktok_system_be.service.Queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petd.tiktok_system_be.api.GetWebhookApi;
import com.petd.tiktok_system_be.api.WebhookApi;
import com.petd.tiktok_system_be.api.body.Event;
import com.petd.tiktok_system_be.dto.message.WebhookMessage;
import com.petd.tiktok_system_be.entity.Auth.Setting;
import com.petd.tiktok_system_be.entity.Auth.SettingSystem;
import com.petd.tiktok_system_be.entity.Manager.Shop;
import com.petd.tiktok_system_be.repository.SettingRepository;
import com.petd.tiktok_system_be.repository.SettingSystemRepository;
import com.petd.tiktok_system_be.repository.ShopRepository;
import com.petd.tiktok_system_be.sdk.TiktokApiResponse;
import com.petd.tiktok_system_be.sdk.appClient.RequestClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class WebhookService {

    RequestClient requestClient;
    KafkaTemplate<String, String> kafkaTemplate;
    ObjectMapper mapper = new ObjectMapper();
    ShopRepository  shopRepository;
    SettingSystemRepository settingSystemRepository;

    public void addWebHook(Event event) throws JsonProcessingException {
        List<Shop> list =  shopRepository.findAll();
        list.forEach(shop -> {
            WebhookMessage msg = WebhookMessage.builder()
                    .event(event)
                    .shopId(shop.getId())
                    .build();
            try {
                kafkaTemplate.send("web-hook", shop.getId(), mapper.writeValueAsString(msg));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void addAllWebHooks() throws JsonProcessingException {
        SettingSystem setting = settingSystemRepository.findAll().get(0);

        Event eventOrder = Event.builder()
                .address(setting.getOrderWebhook())
                .event_type("ORDER_STATUS_CHANGE")
                .build();
        Event eventProduct = Event.builder()
                .address(setting.getProductWebhook())
                .event_type("PRODUCT_STATUS_CHANGE")
                .build();
        Event eventRefund = Event.builder()
                .address(setting.getRefundWebhook())
                .event_type("RETURN_STATUS_CHANGE")
                .build();
        addWebHook(eventOrder);
        addWebHook(eventProduct);
        addWebHook(eventRefund);
    }


    public TiktokApiResponse getWebhook(String shopId) throws JsonProcessingException {
        Shop shop = shopRepository.findById(shopId).get();

        GetWebhookApi getWebhookApi = GetWebhookApi.builder()
                .accessToken(shop.getAccessToken())
                .requestClient(requestClient)
                .shopCipher(shop.getCipher())
                .build();
        return getWebhookApi.callApi();
    }

    @KafkaListener(
            topics = "web-hook",
            containerFactory = "kafkaListenerContainerFactory",
            concurrency = "3"
    )
    public void workerWebhookSync(ConsumerRecord<String, String> record, Acknowledgment ack) throws Exception {
     try {
         WebhookMessage msg = mapper.readValue(record.value(), WebhookMessage.class);

         Shop shop  = shopRepository.findById( msg.getShopId()).get();
         log.info("msg {} {}", msg.getEvent().getEvent_type(), msg.getEvent().getAddress());
         WebhookApi webhookApi = WebhookApi.builder()
                 .shopCipher(shop.getCipher())
                 .accessToken(shop.getAccessToken())
                 .requestClient(requestClient)
                 .body(msg.getEvent())
                 .build();
         webhookApi.callApi();
         ack.acknowledge();
     }catch (Exception e){
         log.error(e.getMessage());
     }
    }
}
