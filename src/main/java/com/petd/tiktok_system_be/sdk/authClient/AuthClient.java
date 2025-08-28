package com.petd.tiktok_system_be.sdk.authClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petd.tiktok_system_be.sdk.TiktokApiResponse;
import com.petd.tiktok_system_be.sdk.exception.TiktokException;
import com.petd.tiktok_system_be.shared.TiktokAuthAppClient;
import org.springframework.beans.factory.annotation.Value;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthClient implements TiktokAuthAppClient {


    final OkHttpClient okHttpClient = new OkHttpClient();
    final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.tiktok.url.auth}")
    String baseUrl;

    @Value("${app.tiktok.app.key}")
    String appKey;

    @Value("${app.tiktok.app.secret}")
    String appSecret;


    @Override
    public TiktokApiResponse getToken(String code) throws TiktokException, IOException {
        HttpUrl url = HttpUrl.parse(baseUrl + "/api/v2/token/get")
                .newBuilder()
                .addQueryParameter("app_key", appKey)
                .addQueryParameter("app_secret", appSecret)
                .addQueryParameter("grant_type", "authorized_code")
                .addQueryParameter("auth_code", code)
                .build();
        return getResponse(url);
    }

    @Override
    public TiktokApiResponse refreshToken (String refreshToken) throws IOException {
        HttpUrl url = HttpUrl.parse(baseUrl + "/api/v2/token/refresh")
                .newBuilder()
                .addQueryParameter("app_key", appKey)
                .addQueryParameter("app_secret", appSecret)
                .addQueryParameter("grant_type", "refresh_token")
                .addQueryParameter("refresh_token", refreshToken)
                .build();
        return getResponse(url);
    }

    private TiktokApiResponse getResponse(HttpUrl url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("Failed to refresh token: HTTP {}", response.code());
                throw TiktokException.builder()
                        .code(400)
                        .message(response.message())
                        .build();
            }
            TiktokApiResponse res = objectMapper.readValue(response.body().string(), TiktokApiResponse.class);
            if(res.getCode() != 0){
                log.error("Failed to refresh token: HTTP {}", res.getMessage());
                throw TiktokException.builder()
                        .code(res.getCode())
                        .message(res.getMessage())
                        .build();
            }
            return res;
        } catch (IOException  e) {
            log.error("Error refreshing token: {}", e.getMessage(), e);
            throw new TiktokException(500, "Lỗi hệ thống!");
        }
    }
}
