package com.petd.tiktok_system_be.service.Product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petd.tiktok_system_be.api.body.SkuBody;
import com.petd.tiktok_system_be.api.body.productRequestUpload.Inventory;
import com.petd.tiktok_system_be.api.body.productRequestUpload.SkuUpload;
import com.petd.tiktok_system_be.dto.message.OrderDetalisSyncMessage;
import com.petd.tiktok_system_be.dto.request.ProductId;
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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
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
    ShopRepository shopRepository;
    ProductService productService;

    public void push (MultipartFile file) throws IOException {
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
                    String productId = productIdCell.getStringCellValue().trim();
                    String shopName = shopNameCell.getStringCellValue().trim();
                    Integer quantity = Integer.parseInt(quantityCell.getStringCellValue().trim());
                    MessageStockUpdate messageStockUpdate = new MessageStockUpdate(shopName,productId,quantity);
                    kafkaTemplate.send("update-stock", productId, objectMapper.writeValueAsString(messageStockUpdate));
                }
            }
        }
    }

    public void update (ConsumerRecord<String, String> record, Acknowledgment ack) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        MessageStockUpdate msg = mapper.readValue(record.value(), MessageStockUpdate.class);

        JsonNode productJson = productService.getProductByShopName(msg.shopName, msg.productId());
        ProductDetailsMap productDetailsMap =  mapper.convertValue(productJson, ProductDetailsMap.class);

        List<SkuUpload> skus = productDetailsMap.getSkus();
        List<SkuBody> skuBodies = new ArrayList<>();
        skus.forEach(sku -> {
            List<Inventory> inventories = sku.getInventory();
            inventories.forEach(inventory -> {
                inventory.setQuantity(msg.quantity);
            });
            skuBodies.add(SkuBody.builder()
                    .sku(sku.getId())
                    .inventory(inventories)
                    .build());
        });




    public record MessageStockUpdate(
            String shopName,
            String productId,
            Integer quantity
    ){}
}
