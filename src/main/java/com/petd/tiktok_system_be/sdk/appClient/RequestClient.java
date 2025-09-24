package com.petd.tiktok_system_be.sdk.appClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petd.tiktok_system_be.sdk.TiktokApiResponse;
import com.petd.tiktok_system_be.sdk.exception.TiktokException;
import com.petd.tiktok_system_be.util.TikTokSignatureUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class RequestClient{

    private final TikTokSignatureUtil signatureUtil;
    private final OkHttpClient okHttpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.tiktok.url.request}")
    String baseUrl;

    @Value("${app.tiktok.app.key}")
    String appKey;

    @Value("${app.tiktok.app.secret}")
    String appSecret;


    public TiktokApiResponse get(String path, String accessToken, Map<String, String> queryParams) {
        return execute("GET", path, accessToken, queryParams, null);
    }

    public TiktokApiResponse post(String path, String accessToken, Map<String, String> queryParams, String jsonBody) {
        return execute("POST", path, accessToken, queryParams, jsonBody);
    }

    public TiktokApiResponse put(String path, String accessToken, Map<String, String> queryParams, String jsonBody) {
        return execute("PUT", path, accessToken, queryParams, jsonBody);
    }
    public TiktokApiResponse delete(String path, String accessToken, Map<String, String> queryParams, String jsonBody) {
        return execute("DELETE", path, accessToken, queryParams, jsonBody );
    }


    private TiktokApiResponse execute(String method, String path, String accessToken, Map<String, String> queryParams, String jsonBody) {
        long timestamp = Instant.now().getEpochSecond();
        Map<String, String> params = new TreeMap<>();
        params.put("app_key", appKey);
        params.put("timestamp", String.valueOf(timestamp));
        if (queryParams != null) params.putAll(queryParams);

        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + path).newBuilder();
        params.forEach(urlBuilder::addQueryParameter);

        RequestBody body = null;
        if ("POST".equalsIgnoreCase(method)  || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)   && jsonBody != null) {
            body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
        }

        Request unsigned = new Request.Builder()
                .url(urlBuilder.build())
                .method(method, body)
                .addHeader("Content-Type", "application/json")
                .addHeader("x-tts-access-token", accessToken)
                .build();

        String sign = signatureUtil.generateSignature(unsigned, appSecret);
        HttpUrl signedUrl = unsigned.url().newBuilder().addQueryParameter("sign", sign).build();

        Request signedRequest = unsigned.newBuilder().url(signedUrl).build();

        try (Response response = okHttpClient.newCall(signedRequest).execute()) {

            if (!response.isSuccessful()) {
                log.error("Failed : HTTP {}", response.body().string());
                throw TiktokException.builder()
                        .code(400)
                        .message(response.body().string())
                        .build();
            }

            TiktokApiResponse res = objectMapper.readValue(response.body().string(), TiktokApiResponse.class);
            if(res.getCode() != 0){
                log.error("{}", res.getCode());
                throw TiktokException.builder()
                        .code(res.getCode())
                        .message(res.getMessage())
                        .build();
            }
            return res;
        } catch (IOException  e) {
            log.error("IO: {}", e.getMessage(), e);
            throw new TiktokException(500, "Lỗi hệ thống!");
        }
    }

    public TiktokApiResponse postMultipart(String path, String accessToken, Map<String, String> queryParams, Map<String, Object> multipartData) {
        long timestamp = Instant.now().getEpochSecond();
        Map<String, String> params = new TreeMap<>();
        params.put("app_key", appKey);
        params.put("timestamp", String.valueOf(timestamp));
        if (queryParams != null) params.putAll(queryParams);

        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + path).newBuilder();
        params.forEach(urlBuilder::addQueryParameter);

        RequestBody requestBody = buildRequestBodyMultipart(multipartData);

        Request unsigned = new Request.Builder()
                .url(urlBuilder.build())
                .post(requestBody)
                .addHeader("x-tts-access-token", accessToken)
                .addHeader("Content-Type", "multipart/form-data")
                .build();

        String sign = signatureUtil.generateSignature(unsigned, appSecret);
        HttpUrl signedUrl = unsigned.url().newBuilder().addQueryParameter("sign", sign).build();
        Request signedRequest = unsigned.newBuilder().url(signedUrl).build();

        try (Response response = okHttpClient.newCall(signedRequest).execute()) {
            if (!response.isSuccessful()) {
                throw new TiktokException(response.code(), response.body().string());
            }
            return objectMapper.readValue(response.body().string(), TiktokApiResponse.class);
        } catch (IOException e) {
            throw new TiktokException(500, "Lỗi hệ thống!");
        }
    }


    public RequestBody buildRequestBodyMultipart(Map<String, Object> formParams) {
        MultipartBody.Builder mpBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        for (Map.Entry<String, Object> param : formParams.entrySet()) {
            String key = param.getKey();
            Object value = param.getValue();

            if (value instanceof File) {
                addPartToMultiPartBuilder(mpBuilder, key, (File) value);
            } else if (value instanceof List<?>) {
                for (Object item : (List<?>) value) {
                    if (item instanceof File) {
                        addPartToMultiPartBuilder(mpBuilder, key, (File) item);
                    } else {
                        mpBuilder.addFormDataPart(key, item.toString());
                    }
                }
            } else {
                mpBuilder.addFormDataPart(key, value.toString());
            }
        }

        return mpBuilder.build();
    }
    private void addPartToMultiPartBuilder(MultipartBody.Builder mpBuilder, String key, File file) {
        Headers partHeaders = Headers.of(new String[]{"Content-Disposition", "form-data; name=\"" + key + "\"; filename=\"" + file.getName() + "\""});
        MediaType mediaType = MediaType.parse(this.guessContentTypeFromFile(file));
        mpBuilder.addPart(partHeaders, RequestBody.create(file, mediaType));
    }


    public String guessContentTypeFromFile(File file) {
        String contentType = URLConnection.guessContentTypeFromName(file.getName());
        return contentType == null ? "application/octet-stream" : contentType;
    }



}
