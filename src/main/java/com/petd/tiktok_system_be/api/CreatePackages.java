package com.petd.tiktok_system_be.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.petd.tiktok_system_be.api.body.CreatePackagesBody;
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
public class CreatePackages implements TiktokCallApi {

    private final String api = "/fulfillment/202309/packages";

    RequestClient requestClient;
    String shopCipher;
    String accessToken;


    CreatePackagesBody body;

    @Override
    public Map<String, String> createParameters() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("shop_cipher", shopCipher);
        return params;
    }

    @Override
    public TiktokApiResponse callApi() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        String bodyJson = mapper.writeValueAsString(body);
        return requestClient.post(api, accessToken, createParameters(), bodyJson);
    }
}
