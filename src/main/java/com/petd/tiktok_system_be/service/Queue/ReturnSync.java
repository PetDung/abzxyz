package com.petd.tiktok_system_be.service.Queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.petd.tiktok_system_be.dto.message.OrderSyncMessage;
import com.petd.tiktok_system_be.dto.message.RefundMessage;
import com.petd.tiktok_system_be.dto.webhook.req.ReturnData;
import com.petd.tiktok_system_be.dto.webhook.req.TtsNotification;
import com.petd.tiktok_system_be.entity.Manager.Shop;
import com.petd.tiktok_system_be.entity.Order.Order;
import com.petd.tiktok_system_be.entity.Return.Return;
import com.petd.tiktok_system_be.repository.OrderRepository;
import com.petd.tiktok_system_be.repository.ReturnRepository;
import com.petd.tiktok_system_be.service.Order.OrderService;
import com.petd.tiktok_system_be.service.Return.ReturnService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReturnSync {

    OrderRepository orderRepository;
    KafkaTemplate<String, String> kafkaTemplate;
    ObjectMapper objectMapper;
    OrderService orderService;
    ReturnService returnService;
    ReturnRepository returnRepository;

    public void pushJob(){
        List<Order> list = orderRepository.findAllByStatus("COMPLETED");
        list.forEach(order -> {
            RefundMessage refundMessage = RefundMessage.builder()
                    .orderId(order.getId())
                    .shopId(order.getShopId())
                    .build();
            try {
                kafkaTemplate.send("refund-sync",order.getId() , objectMapper.writeValueAsString(refundMessage));
                log.info("✅ Pushed order-refund job {}", order.getId());
            } catch (JsonProcessingException e) {
                log.error("❌ Failed to push order-refund job {}: {}", order.getId(), e.getMessage());
            }
        });
    }

    public void pushJobARefund(TtsNotification<ReturnData> ttsNotification) throws JsonProcessingException {
        RefundMessage refundMessage = RefundMessage.builder()
                .orderId(ttsNotification.getData().getOrderId())
                .shopId(ttsNotification.getShopId())
                .refundId(ttsNotification.getData().getReturnId())
                .build();
        try {
            kafkaTemplate.send("refund-sync",ttsNotification.getShopId(), objectMapper.writeValueAsString(refundMessage));
            log.info("✅ Pushed order-refund job {}", ttsNotification.getData().getOrderId());
        } catch (JsonProcessingException e) {
            log.error("❌ Failed to push order-refund job {}: {}", ttsNotification.getData().getOrderId(), e.getMessage());
        }
    }

    @KafkaListener(topics = "refund-sync",
            containerFactory = "kafkaListenerContainerFactory",
            concurrency = "3"
    )
    public void workerOrderSync(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

            RefundMessage msg = objectMapper.readValue(record.value(), RefundMessage.class);

            log.info("⚠️ start order-refund {}", msg.getOrderId());

            Map<String, String> params = new HashMap<>();
            params.put("next_page_token", "");
            params.put("order_id", msg.getOrderId());
            params.put("return_id", msg.getRefundId());

            JsonNode jsonNode = returnService.getReturn(msg.getShopId(), params, 1);

            int totalCount = Optional.ofNullable(jsonNode.get("total_count"))
                    .map(JsonNode::asInt)
                    .orElse(0);

            if (totalCount == 0) {
                log.info("⚠️ No orders for order {}", msg.getOrderId());
                return;
            }

            JsonNode ordersNode = jsonNode.get("return_orders");

            if (ordersNode != null && ordersNode.isArray()) {
                for (JsonNode node : ordersNode) {
                    Return refund = mapper.treeToValue(node, Return.class);
                    try {
                        Order order = orderService.getById(msg.getOrderId());
                        refund.setOrder(order);
                    }catch (Exception e){
                        log.warn("Order {} không tồn tại trong db",  msg.getOrderId());
                        continue;
                    }
                    returnRepository.save(refund);
                }
            }
            log.info("✅ Synced {} order-refund for order {}", totalCount,msg.getOrderId());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("❌ Error processing refund-sync: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }



}
