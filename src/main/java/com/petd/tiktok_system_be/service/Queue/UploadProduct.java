package com.petd.tiktok_system_be.service.Queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petd.tiktok_system_be.api.body.productRequestUpload.ProductUpload;
import com.petd.tiktok_system_be.dto.message.UploadProductMessage;
import com.petd.tiktok_system_be.service.Product.ReupProduct;
import com.petd.tiktok_system_be.service.Product.UploadProductCase;
import com.petd.tiktok_system_be.service.TelegramService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UploadProduct {

    ReupProduct reupProduct;
    KafkaTemplate<String, String> kafkaTemplate;
    ObjectMapper mapper;
    TelegramService telegramService;
    UploadProductCase uploadProductCase;

    public void pushUpload(ProductUpload productUpload, List<String> myShopIds){
       try {
           myShopIds.forEach(myShopId->{
               try {
                   UploadProductMessage msg = UploadProductMessage.builder()
                           .shopId(myShopId)
                           .productUpload(productUpload)
                           .build();
                   kafkaTemplate.send("product-upload", myShopId, mapper.writeValueAsString(msg));
               } catch (JsonProcessingException e) {
                   telegramService.sendMessage(e.getMessage());
                   e.printStackTrace();
               }
           });
       }catch (Exception e){
           telegramService.sendMessage(e.getMessage());
       }
    }

    @KafkaListener(topics = "product-upload",
            containerFactory = "kafkaListenerContainerFactory",
            concurrency = "3"
    )
    public void upload (ConsumerRecord<String, String> record, Acknowledgment ack){
        try{
            UploadProductMessage msg = mapper.readValue(record.value(), UploadProductMessage.class);
            uploadProductCase.uploadProductCase(msg.getProductUpload(), msg.getShopId());
            ack.acknowledge();
        }catch (Exception e){
            e.printStackTrace();
            ack.acknowledge();
        }
    }
}
