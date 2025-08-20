package com.petd.tiktok_system_be.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
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

    @Column
    String userShopName;

    @Column(nullable = false)
    String accessToken;

    @Column(nullable = false)
    Long accessTokenExpiry;

    @Column(nullable = false)
    String refreshToken;

    @Column(nullable = false)
    String cipher;

    @OneToMany(mappedBy = "shop")
    List<Order> orders;

    @ManyToOne
    @JoinColumn(name = "leader_id")
    Account leader;

    @ManyToOne
    @JoinColumn(name = "group_id")
    ShopGroup group;

    @CreatedDate
    @Column(updatable = false)
    LocalDateTime createdAt;

    @LastModifiedDate
    LocalDateTime updatedAt;

}
