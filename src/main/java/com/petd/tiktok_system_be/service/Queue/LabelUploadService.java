package com.petd.tiktok_system_be.service.Queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.drive.model.File;
import com.petd.tiktok_system_be.dto.message.LabelMessage;
import com.petd.tiktok_system_be.entity.Order.Order;
import com.petd.tiktok_system_be.service.GoogleSevice.GoogleDriveService;
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
public class LabelUploadService {

    static final ObjectMapper mapper = new ObjectMapper();
    GoogleDriveService googleDriveService;
    OrderService orderService;

    @KafkaListener(topics = "order-get-label",
            containerFactory = "kafkaListenerContainerFactory",
            concurrency = "3"
    )
    public void job(ConsumerRecord<String, String> record, Acknowledgment ack) throws Exception {
        try {
            LabelMessage msg = mapper.readValue(record.value(), LabelMessage.class);

            Order order = orderService.getById(msg.getOrderId());

            String mimeType = "application/pdf";
            String folderId = "1tT1Syx94e14uNY2J3-2ZP0RFNeGk7SGU";
            File file = googleDriveService.uploadFileFromUrl(msg.getLabel(),mimeType, folderId, msg.getTrackingNumber());
            order.setLabel(file.getWebViewLink());
            orderService.save(order);

            ack.acknowledge();

        }catch(Exception e){
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

    }
}
