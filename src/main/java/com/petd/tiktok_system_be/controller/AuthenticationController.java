package com.petd.tiktok_system_be.controller;
import com.petd.tiktok_system_be.dto.request.LoginRequest;
import com.petd.tiktok_system_be.dto.response.ApiResponse;
import com.petd.tiktok_system_be.dto.response.LoginSuccessResponse;
import com.petd.tiktok_system_be.dto.response.ResponsePage;
import com.petd.tiktok_system_be.entity.Order.Order;
import com.petd.tiktok_system_be.service.Auth.AuthenticationService;
import com.petd.tiktok_system_be.service.Order.OrderService;
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
    OrderService orderService;

    @PostMapping("/login")
    public ApiResponse<LoginSuccessResponse> login(@RequestBody LoginRequest request, HttpServletResponse response)
            throws Exception {
        log.info("Login Request: {}, {}", request.getUsername(), request.getPassword());
        return ApiResponse.<LoginSuccessResponse>builder()
                .result(authenticationService.login(request, response))
                .build();

    }

    @GetMapping("/order-sync")
    public ApiResponse<?> orderSync(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "order_id", required = false) String orderId,
            @RequestParam(name = "shop_ids", required = false) List<String> shopIds,
            @RequestParam(name = "order_status", required = false) String status,
            @RequestParam(name = "shipping_type", required = false) String shippingType
    ){

        log.info("Order Sync Request: {}, {}", page, shopIds.isEmpty());
        // lấy dữ liệu từ DB với filter
        ResponsePage<Order> response = orderService.getAllOrderOnDataBaseByOwnerId(
                orderId,
                shopIds,
                status,
                shippingType,
                page
        );
        return ApiResponse.<ResponsePage<Order>>builder()
                .message("Order sync started")
                .result(response)
                .build();
    }



}
