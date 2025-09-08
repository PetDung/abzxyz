package com.petd.tiktok_system_be.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.petd.tiktok_system_be.dto.request.DeleteProductRequest;
import com.petd.tiktok_system_be.dto.request.ProductId;
import com.petd.tiktok_system_be.dto.response.ApiResponse;
import com.petd.tiktok_system_be.dto.webhook.req.OrderData;
import com.petd.tiktok_system_be.dto.webhook.req.ProductData;
import com.petd.tiktok_system_be.dto.webhook.req.TtsNotification;
import com.petd.tiktok_system_be.sdk.DriveTokenFetcher;
import com.petd.tiktok_system_be.sdk.TiktokApiResponse;
import com.petd.tiktok_system_be.service.*;
import com.petd.tiktok_system_be.service.ExportConfig.OrderExportCase;
import com.petd.tiktok_system_be.service.Queue.OrderSyncService;
import com.petd.tiktok_system_be.service.Queue.ProductDeleteService;
import com.petd.tiktok_system_be.service.Queue.ProductSyncService;
import com.petd.tiktok_system_be.service.Queue.WebhookService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class Webhook {

    OrderSyncService orderSyncService;
    WebhookService webhookService;
    ProductSyncService productSyncService;
    TransactionsService transactionsService;

    @PostMapping("/order")
    public Boolean OrderWebhook(@RequestBody TtsNotification<OrderData> ttsNotification) throws JsonProcessingException {
        orderSyncService.pushJobNotification(ttsNotification.getShopId(), ttsNotification.getData().getOrderId());
        return true;
    }

    @PostMapping("/product/change")
    public Boolean newProductWebhook(@RequestBody TtsNotification<ProductData> ttsNotification) throws JsonProcessingException {
        productSyncService.pushJob(ttsNotification);
        return true;
    }

    @PostMapping("/add")
    public boolean OrderWebhook() throws JsonProcessingException {
        webhookService.addAllWebHooks();
        return true;
    }

    @GetMapping("/test/{shopId}/{orderId}")
    public ApiResponse<JsonNode> test(
            @PathVariable String orderId,
            @PathVariable String shopId
    ){
       return ApiResponse.<JsonNode>builder()
               .result(transactionsService.getTransactionsByOrderId(shopId, orderId))
               .build();
    }

    @GetMapping("/get")
    public TiktokApiResponse get(@RequestParam("shop_id") String shopId) throws JsonProcessingException {
        return webhookService.getWebhook(shopId);
    }

    DriveTokenFetcher driveTokenFetcher;
    @GetMapping("/ref")
    public boolean refToken () throws Exception {
        driveTokenFetcher.getTokenGG();
        return true;
    }

    @GetMapping("/sync-order")
    public boolean getOrder () throws JsonProcessingException {
        orderSyncService.pushJob();
        return true;
    }

    OrderExportCase orderExportCase;

    @GetMapping("/test")
    public ApiResponse<Map<String, String>> exports(@RequestParam List<String> orderIds) {
        return ApiResponse.<Map<String, String>>builder()
                .result(orderExportCase.run(orderIds))
                .build();
    }

    ProductDeleteService productDeleteService;

    @PostMapping("/delete-product")
    public boolean deleteProduct(@RequestParam("file") MultipartFile file) throws IOException {
        DeleteProductRequest request = parseExcelFile(file);
        productDeleteService.pushJob(request);
        return true;
    }

    public DeleteProductRequest parseExcelFile(MultipartFile file) throws IOException {
        List<ProductId> products = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0); // lấy sheet đầu tiên
            boolean firstRow = true;

            for (Row row : sheet) {
                if (firstRow) {
                    firstRow = false; // bỏ qua header
                    continue;
                }

                Cell shopIdCell = row.getCell(0);
                Cell productIdCell = row.getCell(1);

                if (productIdCell != null && shopIdCell != null) {
                    String productId = productIdCell.getStringCellValue().trim();
                    String shopId = shopIdCell.getStringCellValue().trim();
                    if (!productId.isEmpty() && !shopId.isEmpty()) {
                        products.add(new ProductId(productId, shopId));
                    }
                }
            }
        }

        return DeleteProductRequest.builder()
                .productIds(products)
                .build();
    }

}
