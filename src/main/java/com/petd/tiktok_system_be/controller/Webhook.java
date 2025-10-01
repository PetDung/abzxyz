package com.petd.tiktok_system_be.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petd.tiktok_system_be.dto.request.DeleteProductRequest;
import com.petd.tiktok_system_be.dto.request.ProductId;
import com.petd.tiktok_system_be.dto.response.ApiResponse;
import com.petd.tiktok_system_be.dto.webhook.req.OrderData;
import com.petd.tiktok_system_be.dto.webhook.req.ProductData;
import com.petd.tiktok_system_be.dto.webhook.req.ReturnData;
import com.petd.tiktok_system_be.dto.webhook.req.TtsNotification;
import com.petd.tiktok_system_be.entity.Order.Order;
import com.petd.tiktok_system_be.sdk.DriveTokenFetcher;
import com.petd.tiktok_system_be.sdk.TiktokApiResponse;
import com.petd.tiktok_system_be.sdk.printSdk.PrinteesHub.PrintersHub;
import com.petd.tiktok_system_be.service.Order.OrderService;
import com.petd.tiktok_system_be.service.Queue.*;
import com.petd.tiktok_system_be.service.Shop.DesignService;
import com.petd.tiktok_system_be.service.Shop.ShopService;
import com.petd.tiktok_system_be.service.Shop.WarehouseGet;
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
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class Webhook {

    OrderSyncService orderSyncService;
    WebhookService webhookService;
    ProductSyncService productSyncService;
    ReturnSync returnSync;

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

    @PostMapping("/refund/change")
    public Boolean RefundWebhook(@RequestBody TtsNotification<ReturnData> ttsNotification) throws JsonProcessingException {
        returnSync.pushJobARefund(ttsNotification);
        return true;
    }

    @PostMapping("/add")
    public boolean OrderWebhook() throws JsonProcessingException {
        webhookService.addAllWebHooks();
        return true;
    }

    @GetMapping("/get")
    public TiktokApiResponse get(@RequestParam("shop_id") String shopId) throws JsonProcessingException {
        return webhookService.getWebhook(shopId);
    }

    DriveTokenFetcher driveTokenFetcher;
    @GetMapping("/ref")
    public boolean refGG () throws Exception {
        driveTokenFetcher.getTokenGG();
        return true;
    }

    @GetMapping("/sync-order")
    public boolean getOrder () throws JsonProcessingException {
        orderSyncService.pushJob();
        return true;
    }

    ProductDeleteService productDeleteService;

    @PostMapping("/delete-product")
    public boolean deleteProduct(@RequestParam("file") MultipartFile file) throws IOException {
        DeleteProductRequest request = parseExcelFile(file);
        productDeleteService.pushJob(request);
        return true;
    }

    RefreshTokenService refreshTokenService;
    @GetMapping("/ref-token")
    public boolean refToken () throws Exception {
        refreshTokenService.pushJob();
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
    ShopService shopService;

    @GetMapping("/delete-shop/{shopId}")
    public boolean deleteShop(@PathVariable String shopId) throws JsonProcessingException {
        shopService.deleteShopByShopId(shopId);
        return true;
    }


    PrintersHub printeesHub;
    OrderService orderService;

    @GetMapping("/print/{orderId}")
    public ApiResponse<?> print(@PathVariable String orderId) throws IOException {
        Order order = orderService.getById(orderId);
        ObjectMapper mapper = new ObjectMapper();
        return ApiResponse.<Object>builder().
                result(printeesHub.print(order))
                .build();
    }

    DesignService designService;

    @GetMapping("/design/syn")
    public String desginDB() {
        CompletableFuture.runAsync(designService::snyc);
        return "Oke";
    }

    @GetMapping("/design/syn_id")
    public String aDesginDB(@RequestParam String id) {
        CompletableFuture.runAsync(() -> designService.aDesnyc(id));
        return "Oke";
    }

    WarehouseGet warehouse;
    @GetMapping("/warehouse")
    public String warehouse() {
        CompletableFuture.runAsync(warehouse::updateWarehouseForAllShops);
        return "Oke";
    }
}
