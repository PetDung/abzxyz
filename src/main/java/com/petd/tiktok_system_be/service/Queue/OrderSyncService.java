package com.petd.tiktok_system_be.service.Queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.petd.tiktok_system_be.dto.message.OrderDetalisSyncMessage;
import com.petd.tiktok_system_be.dto.message.OrderSyncMessage;
import com.petd.tiktok_system_be.entity.Order.Order;
import com.petd.tiktok_system_be.entity.Manager.Shop;
import com.petd.tiktok_system_be.repository.OrderRepository;
import com.petd.tiktok_system_be.repository.ShopRepository;
import com.petd.tiktok_system_be.service.NotificationService;
import com.petd.tiktok_system_be.service.Order.OrderSaveDataBaseService;
import com.petd.tiktok_system_be.service.Order.OrderService;
import com.petd.tiktok_system_be.service.Order.ShippingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderSyncService {

    KafkaTemplate<String, String> kafkaTemplate;
    ShopRepository shopRepository;
    OrderService orderService;
    OrderSaveDataBaseService orderSaveDataBaseService;
    NotificationService notificationService;
    ShippingService shippingService;

    /**
     * Push job đồng bộ đơn hàng cho tất cả shop
     */
    public void pushJob() {
        List<Shop> shops = shopRepository.findAll();
        ObjectMapper mapper = new ObjectMapper();
        for (Shop shop : shops) {
            try {
                OrderSyncMessage msg = OrderSyncMessage.builder()
                        .shopId(shop.getId())
                        .limit(20)
                        .build();

                kafkaTemplate.send("order-sync", shop.getId(), mapper.writeValueAsString(msg));
                log.info("✅ Pushed order-sync job for shop {}", shop.getId());
            } catch (Exception e) {
                log.error("❌ Failed to push job for shop {}: {}", shop.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * Push job sync chi tiết đơn hàng (thường dùng cho notification)
     */

    OrderRepository orderRepository;

    public void pubJobStatus(String status){
        ObjectMapper mapper = new ObjectMapper();
        List<Order> list = orderRepository.findAllByStatus(status);
        list.forEach(order -> {
            try {
                OrderDetalisSyncMessage msg = OrderDetalisSyncMessage.builder()
                        .shopId(order.getShop().getId())
                        .orderId(order.getId())
                        .build();
                kafkaTemplate.send("order-details-sync", order.getShop().getId(), mapper.writeValueAsString(msg));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void pushJobNotification(String shopId, String orderId) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            OrderDetalisSyncMessage msg = OrderDetalisSyncMessage.builder()
                    .shopId(shopId)
                    .orderId(orderId)
                    .build();

            kafkaTemplate.send("order-details-sync", shopId, mapper.writeValueAsString(msg));
            log.info("✅ Pushed order-details-sync job shopId={} orderId={}", shopId, orderId);
        } catch (Exception e) {
            log.error("❌ Failed to push order-details job: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "order-sync",
            containerFactory = "kafkaListenerContainerFactory",
            concurrency = "3"
    )
    public void workerOrderSync(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            OrderSyncMessage msg = mapper.readValue(record.value(), OrderSyncMessage.class);

            Shop shop = shopRepository.findById(msg.getShopId())
                    .orElseThrow(() -> new IllegalArgumentException("Shop not found: " + msg.getShopId()));

            Map<String, String> params = new HashMap<>();
            params.put("next_page_token", "");

            JsonNode jsonNode = orderService.getOrders(shop.getId(), params, msg.getLimit());

            int totalCount = Optional.ofNullable(jsonNode.get("total_count"))
                    .map(JsonNode::asInt)
                    .orElse(0);

            if (totalCount == 0) {
                log.info("⚠️ No orders for shop {}", shop.getId());
                return;
            }

            JsonNode ordersNode = jsonNode.get("orders");
            if (ordersNode != null && ordersNode.isArray()) {
                for (JsonNode node : ordersNode) {
                    Order order = mapper.treeToValue(node, Order.class);
                    order.setShop(shop);
                    orderSaveDataBaseService.save(order);
                }
            }

            log.info("✅ Synced {} orders for shop {}", totalCount, shop.getId());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("❌ Error processing order-sync: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "order-details-sync",
            containerFactory = "kafkaListenerContainerFactory",
            concurrency = "3"
    )
    public void workerOrderDetailSync(ConsumerRecord<String, String> record,  Acknowledgment ack) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

            OrderDetalisSyncMessage msg = mapper.readValue(record.value(), OrderDetalisSyncMessage.class);
            Shop shop = shopRepository.findById(msg.getShopId())
                    .orElseGet(() -> {
                        log.warn("Không tìm thấy shop {} : {}", msg.getShopId(), msg.getOrderId());
                        ack.acknowledge();
                        return null;
                    });

            if(shop==null) return;

            Map<String, String> params = new HashMap<>();
            params.put("next_page_token", "");
            params.put("order_id", msg.getOrderId());

            JsonNode jsonNode = orderService.getOrders(shop.getId(), params, 1);

            JsonNode ordersNode = jsonNode.get("orders");
            if (ordersNode != null && ordersNode.isArray()) {
                for (JsonNode node : ordersNode) {
                    Order order = mapper.treeToValue(node, Order.class);
                    order.setShop(shop);
                    Order db = orderSaveDataBaseService.save(order);
                    autoGetLabel(db);
                    notificationService.orderUpdateStatus(db);
                }
            }
            log.info("✅ Synced order details shopId={} orderId={}", shop.getId(), msg.getOrderId());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("❌ Error processing order-details-sync: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void autoGetLabel (Order order) throws JsonProcessingException {
        if(!"TIKTOK".equals(order.getShippingType())) return;
        if("AWAITING_SHIPMENT".equals(order.getStatus())){
            shippingService.buyLabel(order.getId(), order.getShopId());
        }
        if("AWAITING_COLLECTION".equals(order.getStatus())){
            shippingService.getLabel(order.getId(), order.getShopId());
        }
    }
}
