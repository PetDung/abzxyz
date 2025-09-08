package com.petd.tiktok_system_be.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.petd.tiktok_system_be.dto.message.OrderSyncMessage;
import com.petd.tiktok_system_be.dto.message.ProductMessage;
import com.petd.tiktok_system_be.dto.webhook.req.ProductData;
import com.petd.tiktok_system_be.dto.webhook.req.TtsNotification;
import com.petd.tiktok_system_be.entity.Product;
import com.petd.tiktok_system_be.entity.Shop;
import com.petd.tiktok_system_be.repository.ProductRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductSyncService {

    KafkaTemplate<String, String> kafkaTemplate;
    ShopService shopService;
    ObjectMapper mapper = new ObjectMapper();
    ProductService productService;
    ProductRepository productRepository;

    public void pushJob (TtsNotification<ProductData> notification) throws JsonProcessingException {

        Shop shop = shopService.getShopByShopId(notification.getShopId());
        ProductMessage productMessage = ProductMessage.builder()
                .productId(notification.getData().getProductId())
                .shopId(shop.getId())
                .event(notification.getData().getStatus())
                .updateTime(notification.getData().getUpdateTime())
                .build();
        kafkaTemplate.send("product-sync", notification.getShopId(), mapper.writeValueAsString(productMessage));
    }


    @Transactional
    @KafkaListener(topics = "product-sync", groupId = "order-workers", concurrency = "3")
    public void handlerProductJob (ConsumerRecord<String, String> record) throws JsonProcessingException {

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            log.info("received record: {}", record.value());
            ProductMessage msg = mapper.readValue(record.value(), ProductMessage.class);

            JsonNode productNode = productService.getProduct(msg.getShopId(), msg.getProductId());

            Shop shop = shopService.getShopByShopId(msg.getShopId());

            Product product = mapper.convertValue(productNode, Product.class);

            System.out.println(product.getTitle());

            String event = msg.getEvent();

            if("PRODUCT_FIRST_PASS_REVIEW".equals(event)) {
                product.setActiveTime(msg.getUpdateTime());
            }
            product.setShop(shop);
            productRepository.save(product);
        }catch (Exception e){
            log.error(e.getMessage());
        }
    }
}
