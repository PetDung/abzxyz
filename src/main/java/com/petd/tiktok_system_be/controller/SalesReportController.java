package com.petd.tiktok_system_be.controller;

import com.petd.tiktok_system_be.dto.response.ApiResponse;
import com.petd.tiktok_system_be.dto.response.ProductReportResponse;
import com.petd.tiktok_system_be.dto_v2.response.CursorPageResponse;
import com.petd.tiktok_system_be.service.Product.SalesReportService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SalesReportController {

    SalesReportService salesReportService;

    /**
     * 📊 API lấy báo cáo doanh số sản phẩm (trang đầu)
     */
    @GetMapping("/product-sales")
    public ApiResponse<ProductReportResponse> getProductSales(
            @RequestParam(required = false) String productId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false, name = "start_time") Long startDate,
            @RequestParam(required = false, name = "end_time") Long endDate
    ) {
        return ApiResponse.<ProductReportResponse>builder()
                .result(salesReportService.getProductSales(productId, productName, startDate, endDate))
                .build();
    }

    /**
     * 📜 API load thêm dữ liệu theo cursor (phân trang vô tận)
     */
    @GetMapping("/product-sales/next-page")
    public ApiResponse<CursorPageResponse<?>> getProductSalesNextPage(
            @RequestParam(required = false, name = "product_id") String productId,
            @RequestParam(required = false, name = "start_time") Long startDate,
            @RequestParam(required = false, name = "end_time") Long endDate,
            @RequestParam(name = "next_cursor", required = false) String nextCursor
    ) {
        return ApiResponse.<CursorPageResponse<?>>builder()
                .result(salesReportService.getProductSalesCursor(productId, startDate, endDate, nextCursor))
                .build();
    }
}
