package com.petd.tiktok_system_be.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.petd.tiktok_system_be.dto.message.OrderDetalisSyncMessage;
import com.petd.tiktok_system_be.dto.message.OrderSyncMessage;
import com.petd.tiktok_system_be.entity.Order;
import com.petd.tiktok_system_be.entity.Shop;
import com.petd.tiktok_system_be.repository.ShopRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderSyncService {

    KafkaTemplate<String, String> kafkaTemplate;
    ShopRepository shopRepository;
    OrderService orderService;
    OrderSaveDataBaseService orderSaveDataBaseService;

    public void pushJob () throws JsonProcessingException {
        List<Shop> shops = shopRepository.findAll();
        ObjectMapper mapper = new ObjectMapper();

        for (Shop shop : shops) {

            OrderSyncMessage msg = OrderSyncMessage.builder()
                    .shopId(shop.getId())
                    .limit(10)
                    .build();

            kafkaTemplate.send("order-sync", shop.getId(), mapper.writeValueAsString(msg));
            System.out.println("Pushed job for shop: " + shop.getId());
        }
    }

    public void pushJobNotication(String shopId, String orderId) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        OrderDetalisSyncMessage msg = OrderDetalisSyncMessage.builder()
                .shopId(shopId)
                .orderId(orderId)
                .build();
        kafkaTemplate.send("order-details-sync", shopId, mapper.writeValueAsString(msg));
    }

    @KafkaListener(topics = "order-sync", groupId = "order-workers", concurrency = "3")
    public void workerOrderSync(ConsumerRecord<String, String> record) throws Exception {
       try {
           TimeUnit.MILLISECONDS.sleep(1000);

           ObjectMapper mapper = new ObjectMapper();
           mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

           OrderSyncMessage msg = mapper.readValue(record.value(), OrderSyncMessage.class);


           Map<String, String> params = new HashMap<>();
           params.put("shop_id", msg.getShopId());
           params.put("next_page_token","");

           Shop shop  = shopRepository.findById( msg.getShopId()).get();

           JsonNode jsonNode = orderService.getOrders(params, msg.getLimit());

           JsonNode totalCountNode  = jsonNode.get("total_count");

           if (totalCountNode != null && totalCountNode.isInt()) {
               int totalCount = totalCountNode.asInt();
               if (totalCount == 0)  return;
           }

           JsonNode ordersNode = jsonNode.get("orders");

           if (ordersNode != null && ordersNode.isArray() && !ordersNode.isEmpty()) {
               // Có dữ liệu order
               for (JsonNode node : ordersNode) {
                   Order order = mapper.treeToValue(node, Order.class);
                   order.setShop(shop);
                   orderSaveDataBaseService.save(order);
               }
           }

       }catch (Exception e) {
           log.error(e.getMessage());
       }
    }

    @KafkaListener(topics = "order-details-sync", groupId = "order-workers")
    public void worderOrderDetailSync (ConsumerRecord<String, String> record) throws InterruptedException, JsonProcessingException {

        try {
            TimeUnit.MILLISECONDS.sleep(1000);
            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            OrderDetalisSyncMessage msg = mapper.readValue(record.value(), OrderDetalisSyncMessage.class);


            Map<String, String> params = new HashMap<>();
            params.put("shop_id", msg.getShopId());
            params.put("next_page_token","");
            params.put("order_id", msg.getOrderId());

            JsonNode jsonNode = orderService.getOrders(params, msg.getLimit());

            JsonNode ordersNode = jsonNode.get("orders");

            Shop shop  = shopRepository.findById( msg.getShopId()).get();

            if (ordersNode != null && ordersNode.isArray() && !ordersNode.isEmpty()) {
                // Có dữ liệu order
                for (JsonNode node : ordersNode) {
                    Order order = mapper.treeToValue(node, Order.class);
                    order.setShop(shop);
                    orderSaveDataBaseService.save(order);
                }
            }
        }catch (Exception e) {
            log.error(e.getMessage());
        }

    }
}
