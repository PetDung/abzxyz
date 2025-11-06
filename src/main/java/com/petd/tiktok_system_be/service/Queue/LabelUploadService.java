package com.petd.tiktok_system_be.service.Queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.drive.model.File;
import com.petd.tiktok_system_be.dto.message.LabelMessage;
import com.petd.tiktok_system_be.entity.Auth.Account;
import com.petd.tiktok_system_be.entity.Auth.Setting;
import com.petd.tiktok_system_be.entity.Order.Order;
import com.petd.tiktok_system_be.service.Auth.SettingService;
import com.petd.tiktok_system_be.service.GoogleSevice.FileFetcherService;
import com.petd.tiktok_system_be.service.GoogleSevice.GoogleDriveUploader;
import com.petd.tiktok_system_be.service.Order.OrderService;
import com.petd.tiktok_system_be.service.Lib.TelegramService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.io.InputStream;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class LabelUploadService {

    static final ObjectMapper mapper = new ObjectMapper();
    GoogleDriveUploader googleDriveUploader;
    FileFetcherService fileFetcherService;
    OrderService orderService;
    TelegramService  telegramService;
    SettingService settingService;

    @KafkaListener(topics = "order-get-label",
            containerFactory = "kafkaListenerContainerFactory",
            concurrency = "3"
    )
    public void job(ConsumerRecord<String, String> record, Acknowledgment ack) throws Exception {
        try {
            LabelMessage msg = mapper.readValue(record.value(), LabelMessage.class);
            Order order = orderService.getById(msg.getOrderId());
            Account account = order.getShop().getLeader();

            Setting setting = settingService.getSetting(account);

            String mimeType = "application/pdf";
            String folderId = setting.getDriverId();

            InputStream uploadFile = fileFetcherService.fromUrl(msg.getLabel());
            File file = googleDriveUploader.uploadOrUpdate(uploadFile,mimeType, folderId, msg.getTrackingNumber());
            String url = file.getWebViewLink();

            order.setLabel(url);
            orderService.save(order);
            ack.acknowledge();
            log.info("Thành công: {}", url);
        }catch(Exception e){
            log.error(e.getMessage());
            telegramService.sendMessage(e.getMessage());
            throw new RuntimeException(e);
        }

    }
}
