package com.petd.tiktok_system_be.entity.Order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.petd.tiktok_system_be.entity.Manager.Printer;
import com.petd.tiktok_system_be.entity.Manager.Shop;
import com.petd.tiktok_system_be.entity.Return.Return;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Order {

    @Id
    String id;
    String trackingNumber;

    String status;

    String buyerMessage;

    String cancelReason;
    String cancellationInitiator;
    String cancelTime;

    Long createTime;
    Long updateTime;

    String deliveryOptionId;
    String deliveryOptionName;
    String deliveryType;

    String fulfillmentType;
    Boolean hasUpdatedRecipientAddress;

    String paymentMethodName;

    BigDecimal paymentAmountActual ;
    BigDecimal paymentAmount;

    String shippingType;
    String shippingProvider;
    String shippingProviderId;
    String warehouseId;

    Boolean isSampleOrder;
    Boolean isCod;

    @Builder.Default
    BigDecimal cost = BigDecimal.ZERO;

    String label;
    Boolean isNote;

    @Transient
    public String getShopName(){
        return shop.getUserShopName();
    }

    @Transient
    public String getShopId(){
        return shop.getId();
    }


    @ManyToOne
    @JoinColumn(name = "printer_id", nullable = true)
    Printer printer;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL,  orphanRemoval = true)
    private PaymentOrder payment;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL,  orphanRemoval = true)
    private Settlement settlement;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL,  orphanRemoval = true)
    private RecipientAddress recipientAddress;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL,  orphanRemoval = true)
    private List<OrderItem> lineItems;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    @JsonIgnore
    Shop shop;

    @OneToMany(mappedBy = "order",  cascade = CascadeType.ALL,  orphanRemoval = true)
    List<Return> returns;

}
