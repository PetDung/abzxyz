package com.petd.tiktok_system_be.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.petd.tiktok_system_be.api.body.Event;
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
public class WebhookApi implements TiktokCallApi {

    private final String api = "/event/202309/webhooks";

    String shopCipher;
    RequestClient requestClient;

    Event body;
    String accessToken;


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
        return requestClient.put(api, accessToken, createParameters(), bodyJson);
    }
}
