package com.petd.tiktok_system_be.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.api.services.drive.model.File;
import com.petd.tiktok_system_be.api.body.Event;
import com.petd.tiktok_system_be.dto.request.DeleteProductRequest;
import com.petd.tiktok_system_be.dto.request.ProductId;
import com.petd.tiktok_system_be.dto.response.ApiResponse;
import com.petd.tiktok_system_be.dto.webhook.req.OrderData;
import com.petd.tiktok_system_be.dto.webhook.req.ProductData;
import com.petd.tiktok_system_be.dto.webhook.req.TtsNotification;
import com.petd.tiktok_system_be.entity.Order;
import com.petd.tiktok_system_be.repository.OrderRepository;
import com.petd.tiktok_system_be.sdk.DriveTokenFetcher;
import com.petd.tiktok_system_be.sdk.TiktokApiResponse;
import com.petd.tiktok_system_be.service.*;
import com.petd.tiktok_system_be.service.Queue.OrderSyncService;
import com.petd.tiktok_system_be.service.Queue.ProductDeleteService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class Webhook {

    OrderSyncService orderSyncService;
    WebhookService webhookService;
    ProductSyncService  productSyncService;
    TransactionsService transactionsService;
    RefreshTokenService  refreshTokenService;
    ProductService productService;

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
    public boolean OrderWebhook(@RequestBody Event event) throws JsonProcessingException {
        webhookService.addWebHook(event);
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

    @GetMapping("/get-product")
    public JsonNode getProduct (@RequestParam String shopId, @RequestParam String productId) throws JsonProcessingException {
        return productService.getProduct(shopId, productId);
    }

    ShippingService shippingService;
    OrderSaveDataBaseService orderSaveDataBaseService;
    OrderRepository orderService;

    @GetMapping("/ship")
    public ResponseEntity<BigDecimal> getShipping(
            @RequestParam String orderId
    ) throws JsonProcessingException {
        Order order = orderService.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        BigDecimal data = orderSaveDataBaseService.paymentAmount(order);
        return ResponseEntity.ok(data);
    }
    GoogleDriveService googleDriveService;

    @PostMapping("/upload-from-url")
    public String uploadFromUrl(
            @RequestParam("fileUrl") String fileUrl,
            @RequestParam("fileName") String fileName,
            @RequestParam(value = "folderId", required = false) String folderId
    ) {
        try {
            File uploadedFile = googleDriveService.uploadFileFromUrl(fileUrl, "application/pdf", folderId, fileName);
            return "Upload thành công! File ID: " + uploadedFile.getId() + ", Link: " + uploadedFile.getWebViewLink();
        } catch (Exception e) {
            log.error("Lỗi upload file từ URL: {}", e.getMessage(), e);
            return "Upload thất bại: " + e.getMessage();
        }
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

                Cell productIdCell = row.getCell(0);
                Cell shopIdCell = row.getCell(1);

                if (productIdCell != null && shopIdCell != null) {
                    String productId = productIdCell.getStringCellValue().trim();
                    String shopId = shopIdCell.getStringCellValue().trim();
                    products.add(new ProductId(productId, shopId));
                }
            }
        }

        return DeleteProductRequest.builder()
                .productIds(products)
                .build();
    }

    public static void main(String[] args) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Products");

        // Header
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("productId");
        header.createCell(1).setCellValue("shopId");

        // Data giả lập 25 sản phẩm
        for (int i = 1; i <= 25; i++) {
            Row row = sheet.createRow(i);
            row.createCell(0).setCellValue("product_" + i);
            row.createCell(1).setCellValue("shopA");
        }

        // Auto size columns
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);

        // Ghi file
        try (FileOutputStream fos = new FileOutputStream("delete_products_test.xlsx")) {
            workbook.write(fos);
        }

        workbook.close();
        System.out.println("File Excel test đã tạo: delete_products_test.xlsx");
    }

}
