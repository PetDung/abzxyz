package com.petd.tiktok_system_be.shared;

import com.petd.tiktok_system_be.sdk.TiktokApiResponse;

import java.io.IOException;

public interface TiktokAuthAppClient {

    String refreshToken(String refreshToken);
    TiktokApiResponse getToken(String code) throws IOException;
}
