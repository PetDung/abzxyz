package com.petd.tiktok_system_be.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.petd.tiktok_system_be.api.GetTransactionsByOrder;
import com.petd.tiktok_system_be.dto.response.ShippingResponse;
import com.petd.tiktok_system_be.entity.*;
import com.petd.tiktok_system_be.sdk.TiktokApiResponse;
import com.petd.tiktok_system_be.sdk.appClient.RequestClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderSaveDataBaseService {

    OrderSaveCase orderSaveCase;
    RequestClient requestClient;
    ShippingService shippingService;
    DesignService designService;
    ObjectMapper mapper = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    public boolean save(Order order) {
        try {
            // --- 1. Lấy settlement từ API (ngoài transaction) ---
            GetTransactionsByOrder getTransactionsByOrder = GetTransactionsByOrder.builder()
                    .orderId(order.getId())
                    .accessToken(order.getShop().getAccessToken())
                    .requestClient(requestClient)
                    .shopCipher(order.getShop().getCipher())
                    .build();

            TiktokApiResponse response = getTransactionsByOrder.callApi();
            // nếu response null hoặc error -> trả false
            if (response == null || response.getData() == null) {
                log.error("Empty response from TikTok for order {}", order.getId());
                return false;
            }

            Settlement settlement = mapper.convertValue(response.getData(), Settlement.class);
            order.setSettlement(settlement);

            // --- 2. Lấy design cho mỗi item (ngoài transaction nếu designService có thể gọi DB safely) ---
            List<OrderItem> items = order.getLineItems();
            List<OrderItem> preprocessedItems = new ArrayList<>();
            if (items != null) {
                for (OrderItem item : items) {
                    // Lưu design vào item (cần tránh lazy-loading problems later)
                    Design design = designService.getDesignBySkuIdAnhProductId(item.getProductId(), item.getSkuId());
                    item.setDesign(design);
                    preprocessedItems.add(item);
                }
            }
            order.setLineItems(preprocessedItems);

            // --- 3. Tính amount (có thể gọi shippingService) ---
            BigDecimal amount = paymentAmount(order); // có thể ném JsonProcessingException
            order.setPaymentAmount(amount);

            // --- 4. Bước DB: chỉ logic persist vào DB, nằm trong transaction ---
            orderSaveCase.persistOrderTransactional(order);
            return true;
        } catch (JsonProcessingException e) {
            log.error("Failed to compute payment amount for order {}: {}", order.getId(), e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("Failed to save order {}: {}", order.getId(), e.getMessage(), e);
            return false;
        }
    }


    public BigDecimal paymentAmount(Order order) throws JsonProcessingException {

        if("ON_HOLD".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) return BigDecimal.ZERO;

        PaymentOrder payment = order.getPayment();
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
