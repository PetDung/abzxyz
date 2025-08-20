package com.petd.tiktok_system_be.service;

import com.petd.tiktok_system_be.entity.*;
import com.petd.tiktok_system_be.repository.*;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderSaveDataBaseService {

    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    PaymentRepository paymentRepository;
    RecipientAddressRepository recipientAddressRepository;

    @Transactional
    public boolean save(Order order) {
        try {
            if (order.getId() != null && orderRepository.existsById(order.getId())) {
                Order existingOrder = orderRepository.findById(order.getId()).get();

                // Remove all children
                existingOrder.setPayment(null);
                existingOrder.setRecipientAddress(null);
                existingOrder.getLineItems().clear();

                // Xóa Order cũ
                orderRepository.delete(existingOrder);
                orderRepository.flush(); // đảm bảo DB xóa trước khi insert mới
            }

            // Gắn entity con mới
            Payment payment = order.getPayment();
            if (payment != null) payment.setOrder(order);

            RecipientAddress addr = order.getRecipientAddress();
            if (addr != null) addr.setOrder(order);

            List<OrderItem> items = order.getLineItems();
            if (items != null) {
                for (OrderItem item : items) {
                    item.setOrder(order);
                }
            }

            // Insert Order mới
            orderRepository.save(order);

            return true;
        } catch (Exception e) {
            log.error("Failed to replace order", e);
            return false;
        }
    }



}
