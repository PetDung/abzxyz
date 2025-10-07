package com.petd.tiktok_system_be.service.PrintCase;

import com.petd.tiktok_system_be.dto.request.SynchronizePrint;
import com.petd.tiktok_system_be.entity.Order.Order;
import com.petd.tiktok_system_be.exception.AppException;
import com.petd.tiktok_system_be.exception.ErrorCode;
import com.petd.tiktok_system_be.sdk.printSdk.PrintSupplier;
import com.petd.tiktok_system_be.sdk.printSdk.PrinteesHub.dto.response.OrderResponse;
import com.petd.tiktok_system_be.service.Lib.NotificationService;
import com.petd.tiktok_system_be.service.Order.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SynchronizePrintCase {

    OrderService orderService;
    HandlePrintOrderCase handlePrintOrderCase;
    NotificationService notificationService;

    public Order synchronize(SynchronizePrint request){
        Order order = orderService.getById(request.getOrderId());
        order.setOrderFulfillId(request.getOrderFulfill());
        order = handlePrintOrderCase.synchronize(order);
        notificationService.orderUpdateStatus(order);
        return order;
    }
}
