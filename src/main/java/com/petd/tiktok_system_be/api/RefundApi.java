package com.petd.tiktok_system_be.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.petd.tiktok_system_be.api.body.RefundBody;
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
public class RefundApi implements TiktokCallApi {

    private final String api = "/return_refund/202309/returns/search";

    RequestClient requestClient;

    String shopCipher;
    String accessToken;

    RefundBody body;


    @Builder.Default
    String sortField  = "create_time";

    @Builder.Default
    String sortOrder = "DESC";

    @Builder.Default
    String pageToken ="";

    @Builder.Default
    int pageSize = 10;


    @Override
    public Map<String, String> createParameters() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("shop_cipher", shopCipher);
        params.put("page_size", String.valueOf(pageSize));
        params.put("sort_order", sortOrder);
        params.put("sort_field", sortField);
        params.put("page_token", pageToken);
        return params;
    }

    @Override
    public TiktokApiResponse callApi() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        String bodyJson = mapper.writeValueAsString(body);
        log.info("bodyJson: {}", bodyJson);
        return requestClient.post(api, accessToken , createParameters(), bodyJson);
    }

}
