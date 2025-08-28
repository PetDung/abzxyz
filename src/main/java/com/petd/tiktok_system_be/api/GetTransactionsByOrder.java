package com.petd.tiktok_system_be.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.petd.tiktok_system_be.api.body.OrderRequestBody;
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
public class GetTransactionsByOrder implements TiktokCallApi {

    private final String api = "/finance/202501/orders/{order_id}/statement_transactions";

    RequestClient requestClient;

    String shopCipher;
    String accessToken;
    String orderId;


    @Override
    public Map<String, String> createParameters() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("shop_cipher", shopCipher);
        return params;
    }

    @Override
    public TiktokApiResponse callApi() throws JsonProcessingException {
        String finalApi = api.replace("{order_id}", orderId);
        return requestClient.get(finalApi, accessToken , createParameters());
    }

}
