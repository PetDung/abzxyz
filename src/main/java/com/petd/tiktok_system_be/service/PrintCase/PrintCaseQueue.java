package com.petd.tiktok_system_be.service.PrintCase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petd.tiktok_system_be.entity.Order.Order;
import com.petd.tiktok_system_be.service.Lib.NotificationService;
import com.petd.tiktok_system_be.service.Order.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PrintCaseQueue {
    NotificationService notificationService;
    OrderService orderService;
    HandlePrintOrderCase handlePrintOrderCase;

    @KafkaListener(topics = "print",
            containerFactory = "kafkaListenerContainerFactory",
            concurrency = "3"
    )
    public void printCaseQueue(ConsumerRecord<String, String> record, Acknowledgment ack) throws JsonProcessingException {
      try {
          ObjectMapper mapper = new ObjectMapper();
          OrderService.MessageOrderPrint msg = mapper.readValue(record.value(), OrderService.MessageOrderPrint.class);

          Order order = orderService.getById(msg.orderId());
          String type = msg.type();

          if("PRINT".equals(type)){
              order = handlePrintOrderCase.printOrder(order);
          }else if("CANCEL".equals(type)){
              order = handlePrintOrderCase.cancel(order);
          }
          notificationService.orderUpdateStatus(order);
          ack.acknowledge();
      }catch (Exception e){
          log.info(e.getMessage());
          throw new RuntimeException(e);
      }
    }

}
