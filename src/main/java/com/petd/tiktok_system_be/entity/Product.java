package com.petd.tiktok_system_be.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.petd.tiktok_system_be.dto.response.ShopResponse;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;


@Entity
@Table(name = "product")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {

    @Id
    String id;
    String title;
    String status;
    Long activeTime;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    List<AuditFailedReason> auditFailedReasons;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    List<MainImage> mainImages;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    List<CategoryChain> categoryChains;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    @JsonIgnore
    Shop shop;

    @Transient
    @JsonProperty("shop")
    public ShopResponse getShopResponse() {
        return ShopResponse.builder()
                .id(shop.getId())
                .userShopName(shop.getUserShopName())
                .tiktokShopName(shop.getTiktokShopName())
                .createdAt(shop.getCreatedAt())
                .build();
    }

    Long createTime;
    Long updateTime;
}
