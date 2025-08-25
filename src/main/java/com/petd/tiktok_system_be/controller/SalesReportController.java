package com.petd.tiktok_system_be.controller;


import com.petd.tiktok_system_be.dto.response.ApiResponse;
import com.petd.tiktok_system_be.dto.response.ProductReportResponse;
import com.petd.tiktok_system_be.service.SalesReportService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SalesReportController {

    SalesReportService salesReportService;

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
}
