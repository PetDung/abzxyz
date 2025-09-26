package com.petd.tiktok_system_be.entity.Manager;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.petd.tiktok_system_be.entity.Auth.Account;
import com.petd.tiktok_system_be.entity.Group.GroupShopAccess;
import com.petd.tiktok_system_be.entity.Group.ShopGroup;
import com.petd.tiktok_system_be.entity.Order.Order;
import com.petd.tiktok_system_be.entity.Payment.Payment;
import com.petd.tiktok_system_be.entity.Product.Product;
import com.petd.tiktok_system_be.entity.Product.UploadedProduct;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Shop {

    @Id
    String id;

    @Column(nullable = false)
    String tiktokShopName;

    @Column(unique = true)
    String userShopName;

    @Column(nullable = false)
    String accessToken;

    @Column(nullable = false)
    Long accessTokenExpiry;

    @Column(nullable = false)
    String refreshToken;

    @Column(nullable = false)
    String cipher;
    String warehouse;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL,  orphanRemoval = true)
    List<Order> orders;

    @ManyToOne
    @JoinColumn(name = "leader_id")
    Account leader;

    @OneToMany(mappedBy = "shop",  cascade = CascadeType.ALL,  orphanRemoval = true)
    List<GroupShopAccess>  groupShopAccess;

    @OneToMany(mappedBy = "shop",  cascade = CascadeType.ALL,  orphanRemoval = true)
    List<Payment>  payments;

    @OneToMany(mappedBy = "shop",  cascade = CascadeType.ALL,  orphanRemoval = true)
    List<Product>  products;

    @OneToMany(mappedBy = "shop",  cascade = CascadeType.ALL,  orphanRemoval = true)
    @JsonIgnore
    List<UploadedProduct> uploadedProducts;

    @CreatedDate
    @Column(updatable = false)
    LocalDateTime createdAt;

    @LastModifiedDate
    LocalDateTime updatedAt;

    @Transient
    public String getOwnerName(){
        return leader.getName();
    }

    @Transient
    public List<String> getProductUpload(){
        if(uploadedProducts == null || uploadedProducts.isEmpty()) return new ArrayList<>();
        return uploadedProducts.stream()
                .map(UploadedProduct::getProductId)
                .toList();
    }
}
