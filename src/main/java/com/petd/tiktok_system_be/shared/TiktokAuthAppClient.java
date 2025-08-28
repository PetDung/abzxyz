package com.petd.tiktok_system_be.shared;

import com.petd.tiktok_system_be.sdk.TiktokApiResponse;

import java.io.IOException;

public interface TiktokAuthAppClient {

    TiktokApiResponse refreshToken(String refreshToken) throws IOException;
    TiktokApiResponse getToken(String code) throws IOException;
}
