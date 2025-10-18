package com.petd.tiktok_system_be.controller;

import com.petd.tiktok_system_be.dto.response.ApiResponse;
import com.petd.tiktok_system_be.dto.response.ResponsePage;
import com.petd.tiktok_system_be.dto.response.ShopResponse;
import com.petd.tiktok_system_be.entity.Manager.Shop;
import com.petd.tiktok_system_be.service.Shop.ShopService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shop")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ShopController {

    ShopService shopService;

    @GetMapping
    public ApiResponse<List<ShopResponse>> getShops(){
        return ApiResponse.<List<ShopResponse>>builder()
                .result(shopService.getMyShopsResponse())
                .build();
    }
    @GetMapping("/details/{id}")
    public ApiResponse<ShopResponse> getShopDetails(@PathVariable String id){
        return ApiResponse.<ShopResponse>builder()
                .result(shopService.getShopResponseByShopId(id))
                .build();
    }


    @GetMapping("/shop-page")
    public ApiResponse<ResponsePage<ShopResponse>> getShopPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ){
        return ApiResponse.<ResponsePage<ShopResponse>>builder()
                .result(shopService.getMyShopPage(page, size))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<ShopResponse> getShopById(
            @PathVariable String id,
            @RequestBody Shop shop
    ) {
        shop.setId(id);
        return ApiResponse.<ShopResponse>builder()
                .result(shopService.update(shop))
                .build();
    }

}
