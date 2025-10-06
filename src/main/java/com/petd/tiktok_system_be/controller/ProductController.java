package com.petd.tiktok_system_be.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.petd.tiktok_system_be.api.body.productRequestUpload.ProductUpload;
import com.petd.tiktok_system_be.dto.response.ApiResponse;
import com.petd.tiktok_system_be.dto.response.ProductResponse;
import com.petd.tiktok_system_be.dto_v2.response.CursorPageResponse;
import com.petd.tiktok_system_be.entity.Product.Product;
import com.petd.tiktok_system_be.service.Product.ProductService;
import com.petd.tiktok_system_be.service.Product.ReupProduct;
import com.petd.tiktok_system_be.service.Queue.UploadProduct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductController {

    ProductService productService;
    UploadProduct uploadProduct;
    ReupProduct reupProduct;


    @GetMapping("/active")
    public ApiResponse<ProductResponse> getActiveProducts(@RequestParam(required = false) Map<String, String> params) {
        params.put("status", "ACTIVATE");
        return ApiResponse.<ProductResponse>builder()
                .result(productService.getListProductInDataBase(params))
                .build();
    }
    @GetMapping("/active/all")
    public ApiResponse<List<Product>> getAllActiveProducts(@RequestParam(required = false) Map<String, String> params) {
        return ApiResponse.<List<Product>>builder()
                .result(productService.getAllActiveProducts(params))
                .build();
    }

    @GetMapping("/active/next-page")
    public ApiResponse<CursorPageResponse<Product>> getActiveProductsCursor(@RequestParam(required = false) Map<String, String> params) {
        return ApiResponse.<CursorPageResponse<Product>>builder()
                .result(productService.getActiveProductCursor(params))
                .build();
    }

    @GetMapping("/record")
    public ApiResponse<ProductResponse> getProductsRecord(@RequestParam(required = false) Map<String, String> params) {
        params.put("status", "FAILED,PLATFORM_DEACTIVATED,FREEZE");
        return ApiResponse.<ProductResponse>builder()
                .result(productService.getListProductInDataBase(params))
                .build();
    }

    @PreAuthorize("@shopSecurity.isAccept(#shopId)")
    @GetMapping("details/{id}/{shopId}")
    public ApiResponse<JsonNode> getProduct (@PathVariable String id, @PathVariable String shopId) throws JsonProcessingException {
        return ApiResponse.<JsonNode>builder()
                .result(productService.getProduct(shopId, id))
                .build();
    }


    @PostMapping("/upload/reup/{productId}/{shopId}")
    public ApiResponse<?> print(@PathVariable String productId,
                                @PathVariable String shopId,
                                @RequestParam List<String> myShopIds) throws IOException {
        ProductUpload productUpload = reupProduct.copyProduct(shopId,productId);
        uploadProduct.pushUpload(productUpload, myShopIds);
        return ApiResponse.<Object>builder().
                result(productUpload)
                .build();
    }
}
