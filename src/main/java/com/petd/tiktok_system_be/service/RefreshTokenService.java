package com.petd.tiktok_system_be.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.petd.tiktok_system_be.dto.message.OrderSyncMessage;
import com.petd.tiktok_system_be.entity.Order;
import com.petd.tiktok_system_be.entity.Shop;
import com.petd.tiktok_system_be.repository.OrderRepository;
import com.petd.tiktok_system_be.repository.ShopRepository;
import com.petd.tiktok_system_be.sdk.TiktokApiResponse;
import com.petd.tiktok_system_be.sdk.appClient.RequestClient;
import com.petd.tiktok_system_be.sdk.exception.TiktokException;
import com.petd.tiktok_system_be.shared.TiktokAuthAppClient;
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
public class RefreshTokenService {

    KafkaTemplate<String, String> kafkaTemplate;
    ShopRepository shopRepository;
    TiktokAuthAppClient tiktokAuthAppClient;

    public void pushJob () throws JsonProcessingException {
        List<Shop> shops = shopRepository.findAll();
        ObjectMapper mapper = new ObjectMapper();
        for (Shop shop : shops) {

            OrderSyncMessage msg = OrderSyncMessage.builder()
                    .shopId(shop.getId())
                    .limit(10)
                    .build();

            kafkaTemplate.send("ref", shop.getId(), mapper.writeValueAsString(msg));
            System.out.println("Pushed job for ref shop: " + shop.getId());
        }
    }

//    @KafkaListener(topics = "ref", groupId = "order-workers", concurrency = "3")
    public void workerOrderSync(ConsumerRecord<String, String> record) throws Exception {

        TimeUnit.MILLISECONDS.sleep(1000);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        OrderSyncMessage msg = mapper.readValue(record.value(), OrderSyncMessage.class);

        Shop shop  = shopRepository.findById( msg.getShopId()).get();

        TiktokApiResponse response = tiktokAuthAppClient.refreshToken(shop.getRefreshToken());

        if(response.getCode() != 0){
            throw new TiktokException(response.getCode(), response.getMessage());
        }

        JsonNode node = response.getData();

        String accessToken = node.get("access_token").asText();
        String refreshToken = node.get("refresh_token").asText();
        Long accessTokenExpiresIn = Long.parseLong(node.get("access_token_expire_in").asText());

        shop.setAccessToken(accessToken);
        shop.setRefreshToken(refreshToken);
        shop.setAccessTokenExpiry(accessTokenExpiresIn);

        shopRepository.save(shop);
    }

}
