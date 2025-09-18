package com.petd.tiktok_system_be.entity.Payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.petd.tiktok_system_be.entity.Manager.Shop;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "payment")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Payment {

    @Id
    String id;

    String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    Money amount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    Money settlementAmount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    Money reserveAmount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    Money paymentAmountBeforeExchange;

    String exchangeRate;

    Long paidTime;

    String bankAccount;

    Long createTime;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    Shop shop;
}