package com.petd.tiktok_system_be.service.Order;

import com.petd.tiktok_system_be.entity.Design.Design;
import com.petd.tiktok_system_be.entity.Order.*;
import com.petd.tiktok_system_be.repository.OrderRepository;
import com.petd.tiktok_system_be.service.Shop.DesignService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderSaveCase {

    OrderRepository orderRepository;
    DesignService designService;

    @Transactional
    public Order persistOrderTransactional(Order order) {

        if (order.getId() != null && orderRepository.existsById(order.getId())) {
            log.info("Order already exists");
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

            PaymentOrder paymentOrder = updateExitedPaymentOrder(existing.getPayment(), order.getPayment());
            Settlement settlement = updateExistingSettlement(existing.getSettlement(), order.getSettlement());
            RecipientAddress recipientAddress = updateExistingRecipientAddress(existing.getRecipientAddress(), order.getRecipientAddress());

            updateLineItems(existing.getLineItems(), order.getLineItems(), existing);
            existing.setSettlement(settlement);
            existing.setPayment(paymentOrder);
            existing.setRecipientAddress(recipientAddress);


            return orderRepository.save(existing);
        } else {
            if (order.getPayment() != null) order.getPayment().setOrder(order);
            if (order.getRecipientAddress() != null) order.getRecipientAddress().setOrder(order);
            if (order.getSettlement() != null) order.getSettlement().setOrder(order);
            if (order.getLineItems() != null) order.getLineItems().forEach(i -> i.setOrder(order));
            return orderRepository.save(order);
        }
    }

    public PaymentOrder updateExitedPaymentOrder(PaymentOrder existing, PaymentOrder incoming) {
        if (existing == null) {
            return incoming;
        }
        if (incoming == null) {
            return existing;
        }

        existing.setCurrency(incoming.getCurrency());
        existing.setOriginalShippingFee(incoming.getOriginalShippingFee());
        existing.setOriginalTotalProductPrice(incoming.getOriginalTotalProductPrice());
        existing.setPlatformDiscount(incoming.getPlatformDiscount());
        existing.setProductTax(incoming.getProductTax());
        existing.setSellerDiscount(incoming.getSellerDiscount());
        existing.setShippingFee(incoming.getShippingFee());
        existing.setShippingFeeCofundedDiscount(incoming.getShippingFeeCofundedDiscount());
        existing.setShippingFeePlatformDiscount(incoming.getShippingFeePlatformDiscount());
        existing.setShippingFeeSellerDiscount(incoming.getShippingFeeSellerDiscount());
        existing.setShippingFeeTax(incoming.getShippingFeeTax());
        existing.setSubTotal(incoming.getSubTotal());
        existing.setTax(incoming.getTax());
        existing.setTotalAmount(incoming.getTotalAmount());

        return existing;
    }

    public Settlement updateExistingSettlement(Settlement existing, Settlement incoming) {
        if (existing == null) {
            return incoming;
        }
        if (incoming == null) {
            return existing;
        }

        existing.setCurrency(incoming.getCurrency());
        existing.setFeeAndTaxAmount(incoming.getFeeAndTaxAmount());
        existing.setOrderCreateTime(incoming.getOrderCreateTime());
        existing.setRevenueAmount(incoming.getRevenueAmount());
        existing.setSettlementAmount(incoming.getSettlementAmount());
        existing.setShippingCostAmount(incoming.getShippingCostAmount());
        return existing;
    }

    public RecipientAddress updateExistingRecipientAddress(RecipientAddress existing, RecipientAddress incoming) {
        if (existing == null) {
            return incoming;
        }
        if (incoming == null) {
            return existing;
        }

        existing.setFirstName(incoming.getFirstName());
        existing.setLastName(incoming.getLastName());
        existing.setFullAddress(incoming.getFullAddress());
        existing.setPhoneNumber(incoming.getPhoneNumber());
        existing.setName(incoming.getName());
        existing.setRegionCode(incoming.getRegionCode());
        existing.setPostalCode(incoming.getPostalCode());
        existing.setAddressLine1(incoming.getAddressLine1());
        existing.setAddressLine2(incoming.getAddressLine2());
        existing.setAddressLine3(incoming.getAddressLine3());
        existing.setAddressLine4(incoming.getAddressLine4());
        existing.setAddressDetail(incoming.getAddressDetail());
        existing.setDistrictInfo(incoming.getDistrictInfo());

        return existing;
    }

    public void updateExistingOrderItem(OrderItem existing, OrderItem incoming) {
        if (existing == null) {
            return;
        }
        if (incoming == null) {
            return;
        }

        existing.setProductId(incoming.getProductId());
        existing.setProductName(incoming.getProductName());
        existing.setSkuId(incoming.getSkuId());
        existing.setSkuName(incoming.getSkuName());
        existing.setSkuImage(incoming.getSkuImage());
        existing.setSkuType(incoming.getSkuType());
        existing.setOriginalPrice(incoming.getOriginalPrice());
        existing.setSalePrice(incoming.getSalePrice());
        existing.setSellerSku(incoming.getSellerSku());
        existing.setPackageStatus(incoming.getPackageStatus());
        existing.setIsGift(incoming.getIsGift());
        existing.setIsDangerousGood(incoming.getIsDangerousGood());
        existing.setNeedsPrescription(incoming.getNeedsPrescription());
    }


    public void updateLineItems(List<OrderItem> existing, List<OrderItem> incomingItems, Order order) {
        if (incomingItems == null) {
            return;
        }
        // Map existing theo id
        Map<String, OrderItem> existingMap = existing.stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(OrderItem::getId, Function.identity()));

        // Tập id của incoming để so sánh
        Set<String> incomingIds = incomingItems.stream()
                .map(OrderItem::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 1. Xóa item nào không còn trong incoming
        existing.removeIf(item -> item.getId() != null && !incomingIds.contains(item.getId()));

        // 2. Cập nhật hoặc thêm mới
        for (OrderItem incoming : incomingItems) {
            if (incoming.getId() != null && existingMap.containsKey(incoming.getId())) {
                // Update field của item cũ
                OrderItem existingItem = existingMap.get(incoming.getId());
                updateExistingOrderItem(existingItem, incoming);
            } else {
                // Thêm item mới
                incoming.setOrder(order);
                existing.add(incoming);
            }
            Design design = designService.getDesignBySkuIdAnhProductId(incoming.getSkuId(), incoming.getProductId());
            incoming.setDesign(design);
        }
    }
}
