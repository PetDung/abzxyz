package com.petd.tiktok_system_be.service.Queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petd.tiktok_system_be.api.GetWebhookApi;
import com.petd.tiktok_system_be.api.WebhookApi;
import com.petd.tiktok_system_be.api.body.Event;
import com.petd.tiktok_system_be.dto.message.WebhookMessage;
import com.petd.tiktok_system_be.entity.Shop;
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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class WebhookService {

    RequestClient requestClient;
    KafkaTemplate<String, String> kafkaTemplate;
    ObjectMapper mapper = new ObjectMapper();
    ShopRepository  shopRepository;

    public void addWebHook(Event event) throws JsonProcessingException {
        List<Shop> list =  shopRepository.findAll();
        list.forEach(shop->{
            WebhookMessage msg  = WebhookMessage.builder()
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


    public TiktokApiResponse getWebhook(String shopId) throws JsonProcessingException {
        Shop shop = shopRepository.findById(shopId).get();

        GetWebhookApi  getWebhookApi = GetWebhookApi.builder()
                .accessToken(shop.getAccessToken())
                .requestClient(requestClient)
                .shopCipher(shop.getCipher())
                .build();
        return getWebhookApi.callApi();
    }

    @KafkaListener(topics = "web-hook", concurrency = "3")
    public void workerWebhookSync(ConsumerRecord<String, String> record) throws Exception {
     try {
         TimeUnit.MILLISECONDS.sleep(1000);

         WebhookMessage msg = mapper.readValue(record.value(), WebhookMessage.class);

         Shop shop  = shopRepository.findById( msg.getShopId()).get();

         WebhookApi webhookApi = WebhookApi.builder()
                 .shopCipher(shop.getCipher())
                 .accessToken(shop.getAccessToken())
                 .requestClient(requestClient)
                 .body(msg.getEvent())
                 .build();
         webhookApi.callApi();
     }catch (Exception e){
         log.error(e.getMessage());
     }
    }
}
