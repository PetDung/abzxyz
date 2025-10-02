package com.petd.tiktok_system_be.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petd.tiktok_system_be.api.body.SkuBody;
import com.petd.tiktok_system_be.sdk.TiktokApiResponse;
import com.petd.tiktok_system_be.sdk.appClient.RequestClient;
import com.petd.tiktok_system_be.shared.TiktokCallApi;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockApi implements TiktokCallApi {

    private final String api = "/product/202309/products/{product_id}/inventory/update";

    String productId;
    RequestClient requestClient;

    String shopCipher;
    String accessToken;
    List<SkuBody> body;

    @Override
    public Map<String, String> createParameters() {
        return Map.of("shop_cipher", shopCipher);
    }
    @Override
    public TiktokApiResponse callApi() throws JsonProcessingException {
        String finalApi = api.replace("{product_id}", productId);
        ObjectMapper objectMapper = new ObjectMapper();
        String bodyJson = objectMapper.writeValueAsString(Map.of("skus", body));
        return requestClient.post(finalApi, accessToken , createParameters(), bodyJson);
    }
}
