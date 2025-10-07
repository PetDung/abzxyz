package com.petd.tiktok_system_be.controller;

import com.petd.tiktok_system_be.dto.request.SynchronizePrint;
import com.petd.tiktok_system_be.dto.response.ApiResponse;
import com.petd.tiktok_system_be.dto.response.ResponsePage;
import com.petd.tiktok_system_be.entity.Order.Order;
import com.petd.tiktok_system_be.service.PrintCase.GetPrintOrder;
import com.petd.tiktok_system_be.service.PrintCase.SynchronizePrintCase;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/print-order")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PrintOrderController {

    GetPrintOrder printCase;
    SynchronizePrintCase  synchronizeCase;

    @GetMapping("/order")
    public ApiResponse<?> orderSync(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "order_id", required = false) String orderId,
            @RequestParam(name = "shop_ids", required = false) List<String> shopIds,
            @RequestParam(name = "print_status",required = false) List<String> printStatus
    ){
        // lấy dữ liệu từ DB với filter
        ResponsePage<Order> response = printCase.getOrderCanPrint(
                orderId,
                shopIds,
                page,
                printStatus
        );

        return ApiResponse.<ResponsePage<Order>>builder()
                .message("Order sync started")
                .result(response)
                .build();
    }

    @GetMapping("/print-shipping-method")
    public ApiResponse<?> printShippingMethod(){
        return ApiResponse.builder()
                .result(printCase.getAll())
                .build();
    }

    @PutMapping("/synchronize")
    public ApiResponse<?> synchronize(@RequestBody SynchronizePrint synchronizePrint){
        return ApiResponse.builder()
                .result(synchronizeCase.synchronize(synchronizePrint))
                .build();
    }
}
