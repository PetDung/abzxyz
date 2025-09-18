package com.petd.tiktok_system_be.entity.Order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "order_payments")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentOrder {

    @Id
    String orderId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "order_id")
    @JsonIgnore
    Order order;

    private String currency;

    @JsonProperty("original_shipping_fee")
    private BigDecimal  originalShippingFee;

    @JsonProperty("original_total_product_price")
    private BigDecimal  originalTotalProductPrice;

    @JsonProperty("platform_discount")
    private BigDecimal  platformDiscount;

    @JsonProperty("product_tax")
    private BigDecimal  productTax;

    @JsonProperty("seller_discount")
    private BigDecimal  sellerDiscount;

    @JsonProperty("shipping_fee")
    private BigDecimal  shippingFee;

    @JsonProperty("shipping_fee_cofunded_discount")
    private BigDecimal  shippingFeeCofundedDiscount;

    @JsonProperty("shipping_fee_platform_discount")
    private BigDecimal  shippingFeePlatformDiscount;

    @JsonProperty("shipping_fee_seller_discount")
    private BigDecimal  shippingFeeSellerDiscount;

    @JsonProperty("shipping_fee_tax")
    private BigDecimal  shippingFeeTax;

    @JsonProperty("sub_total")
    private BigDecimal  subTotal;

    private BigDecimal  tax;

    @JsonProperty("total_amount")
    private BigDecimal  totalAmount;
}
