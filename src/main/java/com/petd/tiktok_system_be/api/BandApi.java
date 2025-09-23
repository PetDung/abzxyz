package com.petd.tiktok_system_be.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.petd.tiktok_system_be.sdk.TiktokApiResponse;
import com.petd.tiktok_system_be.sdk.appClient.RequestClient;
import com.petd.tiktok_system_be.shared.TiktokCallApi;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BandApi  implements TiktokCallApi {

    private final String api = "/product/202309/brands";

    RequestClient requestClient;
    String shopCipher;
    String accessToken;
    String bandName;

    @Override
    public Map<String, String> createParameters() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("shop_cipher", shopCipher);
        params.put("page_size", "100");
        params.put("category_version", "v2");
        params.put("brand_name",bandName);
        return params;
    }

    @Override
    public TiktokApiResponse callApi() throws JsonProcessingException {
        return requestClient.get(api, accessToken, createParameters());
    }
}
