package com.petd.tiktok_system_be.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.petd.tiktok_system_be.dto.response.ApiResponse;
import com.petd.tiktok_system_be.dto.response.ProductResponse;
import com.petd.tiktok_system_be.entity.Product;
import com.petd.tiktok_system_be.service.ProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("/active/all")
    public ApiResponse<List<Product>> getAllActiveProducts() {
        return ApiResponse.<List<Product>>builder()
                .result(productService.getAllActiveProducts())
                .build();
    }
    @GetMapping("/record")
    public ApiResponse<ProductResponse> getProductsRecord(@RequestParam(required = false) Map<String, String> params) {
        params.put("status", "FAILED,PLATFORM_DEACTIVATED,FREEZE");
        return ApiResponse.<ProductResponse>builder()
                .result(productService.getListProductInDataBase(params))
                .build();
    }
    @GetMapping("details/{id}/{shopId}")
    public ApiResponse<JsonNode> getProduct (@PathVariable String id, @PathVariable String shopId) throws JsonProcessingException {
        return ApiResponse.<JsonNode>builder()
                .result(productService.getProduct(shopId, id))
                .build();
    }
}
