package com.petd.tiktok_system_be.controller;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.petd.tiktok_system_be.dto.request.LoginRequest;
import com.petd.tiktok_system_be.dto.response.ApiResponse;
import com.petd.tiktok_system_be.dto.response.LoginSuccessResponse;
import com.petd.tiktok_system_be.dto.response.OrderResponse;
import com.petd.tiktok_system_be.entity.Order;
import com.petd.tiktok_system_be.service.AuthenticationService;
import com.petd.tiktok_system_be.service.OrderService;
import com.petd.tiktok_system_be.service.OrderSyncService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationController {

    AuthenticationService authenticationService;
    OrderSyncService orderSyncService;
    OrderService orderService;

    @PostMapping("/login")
    public ApiResponse<LoginSuccessResponse> login(@RequestBody LoginRequest request, HttpServletResponse response)
            throws Exception {
        log.info("Login Request: {}, {}", request.getUsername(), request.getPassword());
        return ApiResponse.<LoginSuccessResponse>builder()
                .result(authenticationService.login(request, response))
                .build();

    }

    @PostMapping("/order-sync")
    public ApiResponse<?> orderSync(@RequestParam (name = "page") Integer page) throws JsonProcessingException {
        orderSyncService.pushJob();
        return ApiResponse.<OrderResponse>builder()
                .message("Order sync started")
                .result(orderService.getAllOrderOnDataBaseByOwnerId("adsdsad", page))
                .build();
    }

}
