package com.petd.tiktok_system_be.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SimpleHttpClient {

    private final OkHttpClient client;
    private final ObjectMapper mapper;

    public SimpleHttpClient(int timeoutSeconds) {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(timeoutSeconds, TimeUnit.SECONDS) // thời gian kết nối
                .readTimeout(timeoutSeconds, TimeUnit.SECONDS)    // thời gian đọc dữ liệu
                .writeTimeout(timeoutSeconds, TimeUnit.SECONDS)   // thời gian ghi dữ liệu
                .build();
        this.mapper = new ObjectMapper();
    }

    public SimpleHttpClient() {
        this.client = new OkHttpClient();
        this.mapper = new ObjectMapper();
    }

    public String request(
            String url,
            String method,
            Map<String, String> headers,
            String bodyJson
    ) throws IOException {
        RequestBody body = null;

        if (bodyJson != null && !method.equalsIgnoreCase("GET")) {
            body = RequestBody.create(
                    bodyJson, MediaType.get("application/json; charset=utf-8")
            );
        }

        Request.Builder builder = new Request.Builder().url(url);

        // Thêm headers
        if (headers != null) {
            headers.forEach(builder::addHeader);
        }

        // Chọn method
        switch (method.toUpperCase()) {
            case "POST":
                builder.post(body);
                break;
            case "PUT":
                builder.put(body);
                break;
            case "DELETE":
                if (body != null) builder.delete(body);
                else builder.delete();
                break;
            default: // GET
                builder.get();
        }

        try (Response response = client.newCall(builder.build()).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            log.info("Response Body: {}", responseBody);
            if (!response.isSuccessful()) {
                throw new IOException(responseBody);
            }
            return responseBody;
        }
    }
    // Parse response về object
    public <T> T requestForObject(
            String url,
            String method,
            Map<String, String> headers,
            String bodyJson,
            Class<T> responseClass
    ) throws IOException {
        String response = request(url, method, headers, bodyJson);
        return mapper.readValue(response, responseClass);
    }

    public String requestForObject(
            String url,
            String method,
            Map<String, String> headers,
            String bodyJson

    ) throws IOException {
        return request(url, method, headers, bodyJson);
    }
}
