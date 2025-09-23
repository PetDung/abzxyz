package com.petd.tiktok_system_be.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petd.tiktok_system_be.api.body.UploadProductImage;
import com.petd.tiktok_system_be.sdk.TiktokApiResponse;
import com.petd.tiktok_system_be.sdk.appClient.RequestClient;
import com.petd.tiktok_system_be.shared.TiktokCallApi;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class UploadImageApi implements TiktokCallApi {

    private final String api = "/product/202309/images/upload";

    RequestClient requestClient;
    String accessToken;
    String useCase;
    File image;

    @Override
    public Map<String, String> createParameters() {
        return Map.of();
    }

    @Override
    public TiktokApiResponse callApi() throws JsonProcessingException {
        Map<String, Object> body = new HashMap<>();
        body.put("use_case", useCase);
        body.put("data", image);
        return requestClient.postMultipart(api,accessToken,createParameters(), body )  ;
    }
}
