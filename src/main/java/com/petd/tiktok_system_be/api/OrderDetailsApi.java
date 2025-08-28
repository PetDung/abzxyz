package com.petd.tiktok_system_be.api;

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
public class OrderDetailsApi implements TiktokCallApi {

    private final String api = "/order/202309/orders";

    RequestClient requestClient;

    String shopCipher;
    String accessToken;
    String orderId;


    @Override
    public Map<String, String> createParameters() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("shop_cipher", shopCipher);
        params.put("ids", orderId);
        return params;
    }

    @Override
    public TiktokApiResponse callApi() {
        return requestClient.get(api, accessToken , createParameters());
    }

}
