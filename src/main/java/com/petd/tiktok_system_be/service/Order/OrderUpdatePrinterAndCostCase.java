package com.petd.tiktok_system_be.service.Order;

import com.petd.tiktok_system_be.dto.request.UpdateOrderCostPrinter;
import com.petd.tiktok_system_be.entity.Order.Order;
import com.petd.tiktok_system_be.entity.Manager.Printer;
import com.petd.tiktok_system_be.repository.OrderRepository;
import com.petd.tiktok_system_be.repository.PrinterRepository;
import com.petd.tiktok_system_be.service.Lib.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderUpdatePrinterAndCostCase {

    OrderRepository orderRepository;
    PrinterRepository printerRepository;
    NotificationService notificationService;

    @Transactional
    public Map<Integer, List<String>> updatePrinterAndCost(List<UpdateOrderCostPrinter> data) {
        Map<Integer, List<String>> errors = new HashMap<>();
        List<UpdateOrderCostPrinter> validItems = new ArrayList<>();

        // ✅ validate
        for (int i = 0; i < data.size(); i++) {
            UpdateOrderCostPrinter item = data.get(i);
            List<String> itemErrors = new ArrayList<>();

            if (item.getOrderId() == null || item.getOrderId().trim().isEmpty()) {
                itemErrors.add("orderId is blank");
            }

            try {
                String costStr = Optional.ofNullable(item.getCost()).orElse("").trim();
                if (costStr.isEmpty()) {
                    itemErrors.add("cost is blank");
                } else {
                    BigDecimal cost = new BigDecimal(costStr);
                    item.setCostParse(cost);
                }
            } catch (NumberFormatException e) {
                itemErrors.add("cost is not a valid number");
            }

            if (itemErrors.isEmpty()) {
                validItems.add(item); // ✅ hợp lệ
            } else {
                errors.put(i, itemErrors);
            }
        }

        // ✅ lấy orderId từ validItems
        List<String> orderIds = validItems.stream()
                .map(UpdateOrderCostPrinter::getOrderId)
                .filter(Objects::nonNull)
                .toList();

        // ✅ query order từ DB
        List<Order> orders = orderRepository.findByIdIn(orderIds);

        // Map để dễ tra cứu
        Map<String, UpdateOrderCostPrinter> updateMap = validItems.stream()
                .collect(Collectors.toMap(UpdateOrderCostPrinter::getOrderId, item -> item));

        List<String> printerIds = validItems.stream()
                .map(UpdateOrderCostPrinter::getPrinterId)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();

        Map<String, Printer> printerMap = printerRepository.findByIdIn(printerIds).stream()
                .collect(Collectors.toMap(Printer::getId, p -> p));


        // ✅ update dữ liệu
        for (Order order : orders) {
            UpdateOrderCostPrinter updateData = updateMap.get(order.getId());
            if (updateData != null) {
                order.setCost(updateData.getCostParse());
                String pid = updateData.getPrinterId();
                if (pid != null && !pid.isBlank()) {
                    Printer printer = printerMap.get(pid);
                    if (printer != null) {
                        order.setPrinter(printer);
                    }
                }
            }
        }
        // ✅ lưu tất cả orders
        orderRepository.saveAll(orders);
        orders.forEach(notificationService::orderUpdateStatus);
        return errors;
    }
}
