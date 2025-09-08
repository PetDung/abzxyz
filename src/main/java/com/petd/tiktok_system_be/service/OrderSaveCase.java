package com.petd.tiktok_system_be.service;

import com.petd.tiktok_system_be.entity.Order;
import com.petd.tiktok_system_be.entity.OrderItem;
import com.petd.tiktok_system_be.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderSaveCase {

    OrderRepository orderRepository;

    @Transactional
    public void persistOrderTransactional(Order order) {
        // Nếu cần replace order cũ: xóa cũ trước
        if (order.getId() != null && orderRepository.existsById(order.getId())) {
            // Nếu bạn muốn thực sự xóa rồi insert mới:
            orderRepository.deleteById(order.getId());
            orderRepository.flush();
        }

        // Gắn quan hệ 2 chiều trước khi save
        if (order.getPayment() != null) {
            order.getPayment().setOrder(order);
        }
        if (order.getRecipientAddress() != null) {
            order.getRecipientAddress().setOrder(order);
        }
        if (order.getSettlement() != null) {
            order.getSettlement().setOrder(order);
        }
        if (order.getLineItems() != null) {
            for (OrderItem item : order.getLineItems()) {
                item.setOrder(order);
                // design đã set ở bước trước
            }
        }

        // Save (insert/update) order
        orderRepository.save(order);
        // không cần flush bắt buộc; nếu cần đảm bảo write ngay lập tức thì flush()
    }
}
