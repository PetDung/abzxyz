package com.petd.tiktok_system_be.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.petd.tiktok_system_be.api.GetTransactionsByOrder;
import com.petd.tiktok_system_be.dto.response.ShippingResponse;
import com.petd.tiktok_system_be.entity.*;
import com.petd.tiktok_system_be.repository.*;
import com.petd.tiktok_system_be.sdk.TiktokApiResponse;
import com.petd.tiktok_system_be.sdk.appClient.RequestClient;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderSaveDataBaseService {

    OrderRepository orderRepository;
    RequestClient requestClient;
    ShippingService shippingService;
    DesignService designService;

    @Transactional
    public boolean save(Order order) {

        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        try {
            if (order.getId() != null && orderRepository.existsById(order.getId())) {
                Order existingOrder = orderRepository.findById(order.getId()).get();

                // Remove all children
                existingOrder.setPayment(null);
                existingOrder.setRecipientAddress(null);
                existingOrder.getLineItems().clear();
                existingOrder.setSettlement(null);

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
                    String productId = item.getProductId();
                    String skuId = item.getSkuId();
                    Design design = designService.getDesignBySkuIdAnhProductId(productId, skuId);
                    item.setDesign(design);
                }
            }

            GetTransactionsByOrder getTransactionsByOrder = GetTransactionsByOrder.builder()
                    .orderId(order.getId())
                    .accessToken(order.getShop().getAccessToken())
                    .requestClient(requestClient)
                    .shopCipher(order.getShop().getCipher())
                    .build();

            TiktokApiResponse response = getTransactionsByOrder.callApi();


            Settlement settlement = mapper.convertValue(response.getData(), Settlement.class);
            settlement.setOrder(order);

            order.setSettlement(settlement);
            BigDecimal amount = paymentAmount(order);
            order.setPaymentAmount(amount);

            // Insert Order mới
            orderRepository.save(order);

            return true;
        } catch (Exception e) {
            log.error("Failed to replace order", e);
            return false;
        }
    }

    public BigDecimal paymentAmount(Order order) throws JsonProcessingException {

        if("ON_HOLD".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) return BigDecimal.ZERO;

        Payment payment = order.getPayment();
        String targetId = "7208502187360519982";

        BigDecimal labelPrice;

        if("SELLER".equals(order.getShippingType())){
            labelPrice = BigDecimal.ZERO;
        }else {
            JsonNode ship = shippingService.getShipping(order.getId(), order.getShop().getId());
            ObjectMapper mapper = new ObjectMapper();
            ShippingResponse response = mapper.treeToValue(ship, ShippingResponse.class);

            labelPrice = response.getShippingServices().stream()
                    .filter(s -> s.getId().equals(targetId))
                    .map(ShippingResponse.ShippingService::getPrice)
                    .map(p -> new BigDecimal(p.replace("$", "")))
                    .findFirst()
                    .orElse(BigDecimal.ZERO);
        }

        BigDecimal revenueAmount = payment.getOriginalTotalProductPrice()
                .subtract(payment.getSellerDiscount());

        BigDecimal feeAndTaxAmount = (payment.getTotalAmount()
                .add(payment.getPlatformDiscount())
                .subtract(payment.getTax()))
                .multiply(new BigDecimal("0.06"))
                .negate(); // đảo dấu để thành số âm

        BigDecimal shippingCostAmount = payment.getOriginalShippingFee()
                .subtract(payment.getShippingFeePlatformDiscount())
                .subtract(payment.getShippingFeeSellerDiscount())
                .subtract(payment.getShippingFeeCofundedDiscount())
                .subtract(labelPrice) // tiền label bạn phải ứng trước
                .add("SELLER".equals(order.getShippingType()) ? BigDecimal.ZERO : new BigDecimal("0.18"));

        if (shippingCostAmount.compareTo(BigDecimal.ZERO) < 0) {
            shippingCostAmount = BigDecimal.ZERO;
        }

        return revenueAmount
                .add(shippingCostAmount)
                .add(feeAndTaxAmount);
    }

}
