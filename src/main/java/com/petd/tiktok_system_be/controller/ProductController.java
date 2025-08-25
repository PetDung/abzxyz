package com.petd.tiktok_system_be.controller;


import com.petd.tiktok_system_be.dto.response.ApiResponse;
import com.petd.tiktok_system_be.dto.response.ProductResponse;
import com.petd.tiktok_system_be.entity.Product;
import com.petd.tiktok_system_be.service.ProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductController {

    ProductService productService;


    @GetMapping("/active")
    public ApiResponse<ProductResponse> getActiveProducts(@RequestParam(required = false) Map<String, String> params) {
        params.put("status", "ACTIVATE");
        return ApiResponse.<ProductResponse>builder()
                .result(productService.getListProductInDataBase(params))
                .build();
    }
    @GetMapping("/record")
    public ApiResponse<ProductResponse> getProductsRecord(@RequestParam(required = false) Map<String, String> params) {
        params.put("status", "FAILED,PLATFORM_DEACTIVATED,FREEZE");
        return ApiResponse.<ProductResponse>builder()
                .result(productService.getListProductInDataBase(params))
                .build();
    }
}
