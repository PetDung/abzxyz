package com.petd.tiktok_system_be.service.Product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petd.tiktok_system_be.api.StockApi;
import com.petd.tiktok_system_be.api.body.SkuBody;
import com.petd.tiktok_system_be.api.body.productRequestUpload.Inventory;
import com.petd.tiktok_system_be.api.body.productRequestUpload.SkuUpload;
import com.petd.tiktok_system_be.entity.Manager.Shop;
import com.petd.tiktok_system_be.repository.ShopRepository;
import com.petd.tiktok_system_be.sdk.appClient.RequestClient;
import com.petd.tiktok_system_be.service.Product.productDetailsMap.ProductDetailsMap;
import com.petd.tiktok_system_be.service.Shop.ShopService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StockService {

    RequestClient requestClient;
    ShopService shopService;
    KafkaTemplate<String, String> kafkaTemplate;
    ObjectMapper objectMapper;
    ProductService productService;

    public void push (MultipartFile file) throws IOException {
        DataFormatter formatter = new DataFormatter(); // formatter toàn cục

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0); // lấy sheet đầu tiên
            boolean firstRow = true;

            for (Row row : sheet) {
                if (firstRow) {
                    firstRow = false; // bỏ qua header
                    continue;
                }

                Cell shopNameCell = row.getCell(0);
                Cell productIdCell = row.getCell(1);
                Cell quantityCell = row.getCell(2);

                if (productIdCell != null && shopNameCell != null) {
                    String productId = formatter.formatCellValue(productIdCell).trim();
                    String shopName = formatter.formatCellValue(shopNameCell).trim();
                    String quantityStr = formatter.formatCellValue(quantityCell).trim();

                    // convert sang Integer, phòng trường hợp để trống
                    Integer quantity = quantityStr.isEmpty() ? 1000 : Integer.parseInt(quantityStr);

                    MessageStockUpdate messageStockUpdate =
                            new MessageStockUpdate(shopName, productId, quantity);

                    kafkaTemplate.send("update-stock", productId,
                            objectMapper.writeValueAsString(messageStockUpdate));
                }
            }
        }
    }


    @KafkaListener(topics = "update-stock",
            containerFactory = "kafkaListenerContainerFactory",
            concurrency = "3"
    )
    public void update (ConsumerRecord<String, String> record, Acknowledgment ack) throws JsonProcessingException {
       try{
           ObjectMapper mapper = new ObjectMapper();
           MessageStockUpdate msg = mapper.readValue(record.value(), MessageStockUpdate.class);

           Shop shop = shopService.getByShopName(msg.shopName);
           JsonNode productJson = productService.getProductByShopName(msg.shopName, msg.productId());

           ProductDetailsMap productDetailsMap = mapper.convertValue(productJson, ProductDetailsMap.class);

           List<SkuUpload> skus = productDetailsMap.getSkus();
           List<SkuBody> skuBodies = new ArrayList<>();
           skus.forEach(sku -> {
               List<Inventory> inventories = sku.getInventory();
               inventories.forEach(inventory -> {
                   inventory.setQuantity(msg.quantity);
               });
               skuBodies.add(SkuBody.builder()
                       .id(sku.getId())
                       .inventory(inventories)
                       .build());
           });

           log.info(mapper.writeValueAsString(skuBodies));

           StockApi stockApi = StockApi.builder()
                   .accessToken(shop.getAccessToken())
                   .body(skuBodies)
                   .productId(msg.productId())
                   .shopCipher(shop.getCipher())
                   .requestClient(requestClient)
                   .build();

           stockApi.callApi();
           log.info("Updated stock product {}", msg.productId());
           ack.acknowledge();
       }catch(Exception e){
           log.error("Failed to upload stock record: {}", record.value(), e);
           throw new RuntimeException(e);
       }

    }
    public record MessageStockUpdate(
            String shopName,
            String productId,
            Integer quantity
    ){}
}
