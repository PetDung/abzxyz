package com.petd.tiktok_system_be.api;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petd.tiktok_system_be.api.body.productRequestUpload.ProductUpload;
import com.petd.tiktok_system_be.sdk.TiktokApiResponse;
import com.petd.tiktok_system_be.sdk.appClient.RequestClient;
import com.petd.tiktok_system_be.shared.TiktokCallApi;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadProductApi implements TiktokCallApi {

    private final String api = "/product/202309/products";
    RequestClient requestClient;
    String accessToken;
    String shopCipher;

    ProductUpload body;

    @Override
    public Map<String, String> createParameters() {
        return Map.of("shop_cipher", shopCipher);
    }

    @Override
    public TiktokApiResponse callApi() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String bodyJson = objectMapper.writeValueAsString(body);
        return requestClient.post( api, accessToken, createParameters(), bodyJson )  ;
    }
}
