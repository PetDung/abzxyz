package com.petd.tiktok_system_be.controller;

import com.petd.tiktok_system_be.dto.request.DesignMappingRequest;
import com.petd.tiktok_system_be.dto.request.DesignRequest;
import com.petd.tiktok_system_be.dto.response.ApiResponse;
import com.petd.tiktok_system_be.entity.Design.Design;
import com.petd.tiktok_system_be.entity.Design.MappingDesign;
import com.petd.tiktok_system_be.service.Shop.DesignService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/design")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DesignController {

    DesignService designService;

    @PostMapping
    public ApiResponse<Design> createDesign(@RequestBody DesignRequest request) {
        return ApiResponse.<Design>builder()
                .result(designService.create(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<Design>> getDesigns() {
        return ApiResponse.<List<Design>>builder()
                .result(designService.getAllDesigns())
                .build();
    }

    @GetMapping("/get-design-by-sku-product")
    public ApiResponse<Design> checkDesign(
            @RequestParam(name = "sku_id") String skuId,
            @RequestParam(name = "product_id") String productId
    ) {
        return ApiResponse.<Design>builder()
                .result(designService.getDesignBySkuIdAnhProductId(skuId, productId))
                .build();
    }


    @GetMapping("/get-design-by-batch-sku-product")
    public ApiResponse<Map<String, Design>> checkDesigns(
            @RequestParam(name = "sku_ids") String[] skuIds,
            @RequestParam(name = "product_id") String productId
    ) {
        return ApiResponse.<Map<String, Design>>builder()
                .result(designService.getDesignBySkusIdAndProductId(skuIds, productId))
                .build();
    }

    @PostMapping("/mapping-design")
    public ApiResponse<MappingDesign> createDesignMapping(@RequestBody DesignMappingRequest request) {
        return ApiResponse.<MappingDesign>builder()
                .result(designService.mappingDesignAndProduct(request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteDesignById(@PathVariable String id) {
        designService.deleteDesignById(id);
        return ApiResponse.<Boolean>builder()
                .result(true)
                .build();
    }

    @DeleteMapping("/remove-skus")
    public ApiResponse<String> removeSkus(
            @RequestParam String productId,
            @RequestParam List<String> skusToRemove
    ) {
        designService.removeSkus(productId, skusToRemove);
        return ApiResponse.<String>builder()
                .result("Delete successfully")
                .build();
    }

}
