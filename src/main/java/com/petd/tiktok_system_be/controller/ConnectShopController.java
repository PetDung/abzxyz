package com.petd.tiktok_system_be.controller;

import com.petd.tiktok_system_be.dto.request.AuthShopRequest;
import com.petd.tiktok_system_be.dto.response.ApiResponse;
import com.petd.tiktok_system_be.dto.response.AuthShopResponse;
import com.petd.tiktok_system_be.service.Shop.ShopService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ConnectShopController {

    ShopService shopService;

    @PostMapping("/connect")
    public ApiResponse<AuthShopResponse> connectShop(@RequestBody(required = true) AuthShopRequest authShop) {
        return ApiResponse.<AuthShopResponse>builder()
                .result(shopService.connectShopSystem(authShop))
                .build();
    }

    @PostMapping("/connect-api")
    public ApiResponse<AuthShopResponse> connectShopApi(@RequestBody(required = true) AuthShopRequest authShop) {
        return ApiResponse.<AuthShopResponse>builder()
                .result(shopService.connectShop(authShop))
                .build();
    }


    @GetMapping("/check-owner")
    public Boolean checkShopOwner(@RequestParam("shopId") String shopId, @RequestParam("accountId")  String accountId) {
        return  shopService.checkShopBelongUser(accountId, shopId);
    }



}
