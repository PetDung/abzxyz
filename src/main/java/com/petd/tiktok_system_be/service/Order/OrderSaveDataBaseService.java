package com.petd.tiktok_system_be.service.Order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.petd.tiktok_system_be.api.GetTransactionsByOrder;
import com.petd.tiktok_system_be.dto.message.RefundMessage;
import com.petd.tiktok_system_be.dto.response.ShippingResponse;
import com.petd.tiktok_system_be.entity.Design.Design;
import com.petd.tiktok_system_be.entity.Order.Order;
import com.petd.tiktok_system_be.entity.Order.OrderItem;
import com.petd.tiktok_system_be.entity.Order.PaymentOrder;
import com.petd.tiktok_system_be.entity.Order.Settlement;
import com.petd.tiktok_system_be.sdk.TiktokApiResponse;
import com.petd.tiktok_system_be.sdk.appClient.RequestClient;
import com.petd.tiktok_system_be.service.Shop.DesignService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
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
    KafkaTemplate<String, String> kafkaTemplate;
    ObjectMapper objectMapper = new ObjectMapper();

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

            if("COMPLETED".equals(order.getStatus())){
                try {
                    RefundMessage refundMessage = RefundMessage.builder()
                            .orderId(order.getId())
                            .shopId(order.getShopId())
                            .build();
                    kafkaTemplate.send("refund-sync",order.getId() , objectMapper.writeValueAsString(refundMessage));
                    log.info("✅ Pushed order-refund job {}", order.getId());
                } catch (JsonProcessingException e) {
                    log.error("❌ Failed to push order-refund job {}: {}", order.getId(), e.getMessage());
                }
            }
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
                .subtract(labelPrice)
                .add("SELLER".equals(order.getShippingType()) ? BigDecimal.ZERO : new BigDecimal("0.18"));


        if(payment.getShippingFeePlatformDiscount().compareTo(BigDecimal.ZERO) > 0){

            shippingCostAmount = shippingCostAmount.subtract(payment.getShippingFeePlatformDiscount());

            if (shippingCostAmount.compareTo(BigDecimal.ZERO) < 0 ) {
                shippingCostAmount = BigDecimal.ZERO;
            }
        }else if(payment.getShippingFeeSellerDiscount().compareTo(BigDecimal.ZERO) > 0){

            shippingCostAmount = shippingCostAmount.subtract(payment.getShippingFeeSellerDiscount());

        }

        return revenueAmount
                .add(shippingCostAmount)
                .add(feeAndTaxAmount);
    }

}
