package com.petd.tiktok_system_be.sdk.printSdk.Monkeykingprint;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petd.tiktok_system_be.entity.Order.Order;
import com.petd.tiktok_system_be.sdk.SimpleHttpClient;
import com.petd.tiktok_system_be.sdk.printSdk.PrintSupplier;
import com.petd.tiktok_system_be.sdk.printSdk.PrinteesHub.dto.response.OrderResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class MonkeyPrint implements PrintSupplier {
    SimpleHttpClient httpClient = new SimpleHttpClient();
    ObjectMapper objectMapper = new ObjectMapper();
    String baseUrl = "https://monkeykingprint.com";
    @Override
    public OrderResponse print(Order order) throws IOException {
        System.out.println(getToken());
        return null;
    }

    @Override
    public String getCode() {
        return "MKP";
    }

    @Override
    public OrderResponse cancel(Order order) throws IOException {
        return null;
    }


    private String getToken () throws IOException {
        String username = "nguyenkhang1000199x@gmail.com";
        String password = "THJ444dbdg$#5j";

        String api ="/rest/V1/integration/customer/token";

        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);

        String bodyJson = objectMapper.writeValueAsString(body);
        return  httpClient.requestForObject(
                baseUrl + api,
                "POST",
                null,
                bodyJson,
                String.class
        );
    }

    private  Map<String, String> getHeader() throws IOException {
        return Map.of(
                "Token", getToken(),
                "Accept", "application/json"
        );
    }
}
