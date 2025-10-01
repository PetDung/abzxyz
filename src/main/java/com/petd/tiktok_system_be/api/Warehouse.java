package com.petd.tiktok_system_be.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.petd.tiktok_system_be.sdk.TiktokApiResponse;
import com.petd.tiktok_system_be.sdk.appClient.RequestClient;
import com.petd.tiktok_system_be.shared.TiktokCallApi;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Warehouse implements TiktokCallApi {


    private final String api = "/logistics/202309/warehouses";

    RequestClient requestClient;

    String shopCipher;
    String accessToken;

    @Override
    public Map<String, String> createParameters() {
        return Map.of("shop_cipher", shopCipher);
    }

    @Override
    public TiktokApiResponse callApi() throws JsonProcessingException {
        return requestClient.get(api, accessToken , createParameters());
    }
}
