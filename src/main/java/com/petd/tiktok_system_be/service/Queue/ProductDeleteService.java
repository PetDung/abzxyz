package com.petd.tiktok_system_be.service.Queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petd.tiktok_system_be.api.DeleteProductApi;
import com.petd.tiktok_system_be.dto.message.DeleteProductMessage;
import com.petd.tiktok_system_be.dto.request.DeleteProductRequest;
import com.petd.tiktok_system_be.dto.request.ProductId;
import com.petd.tiktok_system_be.entity.Shop;
import com.petd.tiktok_system_be.sdk.appClient.RequestClient;
import com.petd.tiktok_system_be.service.ShopService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductDeleteService {

    ObjectMapper mapper;
    ShopService shopService;
    RequestClient requestClient;
    KafkaTemplate<String, String> kafkaTemplate;

    // --- Producer ---
    public void pushJob(DeleteProductRequest request) {
        List<DeleteProductMessage> messages = batchByShop(request, 20);
        for (DeleteProductMessage message : messages) {
            try {
                String payload = mapper.writeValueAsString(message);
                System.out.println("payload: " + payload);
                kafkaTemplate.send("delete-product", payload);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize DeleteProductMessage for shop {}: {}",
                        message.getShopId(), e.getMessage(), e);
            }
        }
    }

    // --- Listener ---
    @KafkaListener(topics = "delete-product",
            containerFactory = "kafkaListenerContainerFactory",
            concurrency = "3"
    )
    public void handleDeleteProduct(ConsumerRecord<String, String> record,  Acknowledgment ack) {
        try {
            DeleteProductMessage msg = mapper.readValue(record.value(), DeleteProductMessage.class);
            processDeleteProduct(msg);
            ack.acknowledge();
            log.info("Deleted products from shop {}", msg.getShopId());
        } catch (Exception e) {
            log.error("Failed to process delete product message: {}", record.value(), e);
            throw new RuntimeException(e);
        }
    }

    private void processDeleteProduct(DeleteProductMessage msg) throws Exception {
        Shop shop = shopService.getShopByShopName(msg.getShopId().trim());
        DeleteProductApi deleteProductApi = DeleteProductApi.builder()
                .body(Map.of("product_ids", msg.getProductIds()))
                .shopCipher(shop.getCipher())
                .accessToken(shop.getAccessToken())
                .requestClient(requestClient)
                .build();
        deleteProductApi.callApi();
    }



    public List<DeleteProductMessage> batchByShop(DeleteProductRequest products, int batchSize) {
        List<ProductId> productId = products.getProductIds();

        // 1. Nhóm theo shopId
        Map<String, List<ProductId>> groupedByShop = productId.stream()
                .collect(Collectors.groupingBy(ProductId::getShopId));

        List<DeleteProductMessage> result = new ArrayList<>();

        // 2. Chia từng nhóm theo batchSize
        for (Map.Entry<String, List<ProductId>> entry : groupedByShop.entrySet()) {
            String shopId = entry.getKey();
            List<ProductId> list = entry.getValue();

            for (int i = 0; i < list.size(); i += batchSize) {
                List<String> batchIds = list.subList(i, Math.min(i + batchSize, list.size()))
                        .stream()
                        .map(ProductId::getProductId)
                        .collect(Collectors.toList());

                DeleteProductMessage message = DeleteProductMessage.builder()
                        .shopId(shopId)
                        .productIds(batchIds)
                        .build();

                result.add(message);
            }
        }
        return result;
    }
}
