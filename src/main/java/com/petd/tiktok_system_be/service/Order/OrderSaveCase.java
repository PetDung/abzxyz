package com.petd.tiktok_system_be.service.Order;

import com.petd.tiktok_system_be.entity.Order.Order;
import com.petd.tiktok_system_be.entity.Order.OrderItem;
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
    public Order persistOrderTransactional(Order order) {

        if (order.getId() != null && orderRepository.existsById(order.getId())) {

            Order existing = orderRepository.findById(order.getId()).orElseThrow();

            // --- Copy field primitive ---
            existing.setTrackingNumber(order.getTrackingNumber());
            existing.setStatus(order.getStatus());
            existing.setBuyerMessage(order.getBuyerMessage());
            existing.setCancelReason(order.getCancelReason());
            existing.setCancellationInitiator(order.getCancellationInitiator());
            existing.setCancelTime(order.getCancelTime());
            existing.setCreateTime(order.getCreateTime());
            existing.setUpdateTime(order.getUpdateTime());
            existing.setDeliveryOptionId(order.getDeliveryOptionId());
            existing.setDeliveryOptionName(order.getDeliveryOptionName());
            existing.setDeliveryType(order.getDeliveryType());
            existing.setFulfillmentType(order.getFulfillmentType());
            existing.setHasUpdatedRecipientAddress(order.getHasUpdatedRecipientAddress());
            existing.setPaymentMethodName(order.getPaymentMethodName());
            existing.setPaymentAmount(order.getPaymentAmount());
            existing.setShippingType(order.getShippingType());
            existing.setShippingProvider(order.getShippingProvider());
            existing.setShippingProviderId(order.getShippingProviderId());
            existing.setWarehouseId(order.getWarehouseId());
            existing.setIsSampleOrder(order.getIsSampleOrder());
            existing.setIsCod(order.getIsCod());
            existing.setUpdateTime(order.getUpdateTime());

            existing.setPayment(order.getPayment());
            existing.setRecipientAddress(order.getRecipientAddress());
            existing.setLineItems(order.getLineItems());
            existing.setSettlement(order.getSettlement());

            if (existing.getPayment() != null) {
                existing.getPayment().setOrder(existing);
            }
            if (existing.getRecipientAddress() != null) {
                existing.getRecipientAddress().setOrder(existing);
            }
            if (existing.getSettlement() != null) {
                existing.getSettlement().setOrder(existing);
            }
            if (existing.getLineItems() != null) {
                for (OrderItem item : existing.getLineItems()) {
                    item.setOrder(existing);
                }
            }
            return orderRepository.save(existing);
        } else {
            if (order.getPayment() != null) order.getPayment().setOrder(order);
            if (order.getRecipientAddress() != null) order.getRecipientAddress().setOrder(order);
            if (order.getSettlement() != null) order.getSettlement().setOrder(order);
            if (order.getLineItems() != null) order.getLineItems().forEach(i -> i.setOrder(order));
            return orderRepository.save(order);
        }
    }
}
