package com.petd.tiktok_system_be.controller;

import com.petd.tiktok_system_be.dto.response.ApiResponse;
import com.petd.tiktok_system_be.dto.response.ResponsePage;
import com.petd.tiktok_system_be.entity.Order.Order;
import com.petd.tiktok_system_be.service.PrintCase.PrintCase;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/print-order")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PrintOrderController {

    PrintCase printCase;

    @GetMapping("/order")
    public ApiResponse<?> orderSync(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "order_id", required = false) String orderId,
            @RequestParam(name = "shop_ids", required = false) List<String> shopIds
    ){
        // lấy dữ liệu từ DB với filter
        ResponsePage<Order> response = printCase.getOrderCanPrint(
                orderId,
                shopIds,
                page
        );
        return ApiResponse.<ResponsePage<Order>>builder()
                .message("Order sync started")
                .result(response)
                .build();
    }
}
