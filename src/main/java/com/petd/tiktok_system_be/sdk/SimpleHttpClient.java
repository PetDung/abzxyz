package com.petd.tiktok_system_be.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;

public class SimpleHttpClient {

    private final OkHttpClient client;
    private final ObjectMapper mapper;

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
            if (!response.isSuccessful()) {
                throw new IOException("HTTP error: " + response.code() + " - " + response.message());
            }
            return response.body().string();
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
}
