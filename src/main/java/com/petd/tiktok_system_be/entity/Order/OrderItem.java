package com.petd.tiktok_system_be.entity.Order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.petd.tiktok_system_be.entity.Design.Design;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;


@Entity
@Table(name = "order_items")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItem {

    @Id
    String id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonIgnore
    Order order;

    String productId;
    @Column(name = "product_name", columnDefinition = "varchar")
    String productName;

    String skuId;
    String skuName;
    String skuImage;
    String skuType;

    BigDecimal originalPrice;
    BigDecimal salePrice;

    String sellerSku;
    String packageStatus;
    Boolean isGift;
    Boolean isDangerousGood;
    Boolean needsPrescription;

    @ManyToOne
    @JoinColumn(name = "design_id")
    Design design;

}
