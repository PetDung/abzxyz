package com.petd.tiktok_system_be.service.Queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.petd.tiktok_system_be.dto.message.ProductMessage;
import com.petd.tiktok_system_be.dto.webhook.req.ProductData;
import com.petd.tiktok_system_be.dto.webhook.req.TtsNotification;
import com.petd.tiktok_system_be.entity.Product.Product;
import com.petd.tiktok_system_be.entity.Manager.Shop;
import com.petd.tiktok_system_be.repository.ProductRepository;
import com.petd.tiktok_system_be.repository.ShopRepository;
import com.petd.tiktok_system_be.service.Product.ProductService;
import com.petd.tiktok_system_be.service.Shop.ShopService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductSyncService {

    KafkaTemplate<String, String> kafkaTemplate;
    ShopService shopService;
    ProductService productService;
    ProductRepository productRepository;
    ShopRepository shopRepository;
    ObjectMapper mapper = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

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
    @KafkaListener(topics = "product-sync",
            containerFactory = "kafkaListenerContainerFactory",
            concurrency = "3"
    )
    public void handlerProductJob (ConsumerRecord<String, String> record,  Acknowledgment ack){
        try {
            log.info("received record: {}", record.value());
            ProductMessage msg = mapper.readValue(record.value(), ProductMessage.class);

            Shop shop = shopRepository.findById(msg.getShopId())
                    .orElseGet(() -> {
                        log.warn("Không tìm thấy shop {} : {}", msg.getShopId(), msg.getProductId());
                        ack.acknowledge();
                        return null;
            });
            if(shop==null) return;

            JsonNode productNode = productService.getProduct(msg.getShopId(), msg.getProductId());

            Product product = mapper.convertValue(productNode, Product.class);
            product.setActiveTime(msg.getUpdateTime());
            product.setShop(shop);
            productRepository.save(product);
            ack.acknowledge();
        }catch (Exception e) {
            log.error("Failed to process delete product message: {}", record.value(), e);
            throw new RuntimeException(e);
        }
    }
}
