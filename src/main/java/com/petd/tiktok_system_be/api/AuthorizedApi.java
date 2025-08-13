package com.petd.tiktok_system_be.api;

import com.petd.tiktok_system_be.sdk.TiktokApiResponse;
import com.petd.tiktok_system_be.sdk.appClient.RequestClient;
import com.petd.tiktok_system_be.shared.TiktokCallApi;
import lombok.*;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorizedApi implements TiktokCallApi {

    private final String api = "/authorization/202309/shops";

    RequestClient requestClient;
    String accessToken;


    @Override
    public Map<String, String> createParameters() {
        return Map.of();
    }

    @Override
    public TiktokApiResponse callApi() {
        return requestClient.get(api, accessToken, null);
    }
}
