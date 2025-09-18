package com.petd.tiktok_system_be.entity.Return;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.petd.tiktok_system_be.entity.Order.Order;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "return")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Return {

    @Id
    String returnId;
    String returnType;
    String returnStatus;
    String arbitrationStatus;
    String role;
    String returnReason;
    String returnReasonText;
    String shipmentType;
    String handoverMethod;
    String returnTrackingNumber;
    String returnProviderName;
    String returnProviderId;
    String preReturnId;
    String nextReturnId;
    Boolean canBuyerKeepItem;
    String returnShippingDocumentType;
    String returnMethod;
    Boolean isCombinedReturn;
    String combinedReturnId;
    String sellerProposedReturnType;
    Boolean buyerRejectedPartialRefund;

    Long updateTime;
    Long createTime;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonIgnore
    Order order;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    RefundAmount refundAmount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    ReturnWarehouseAddress returnWarehouseAddress;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    PartialRefund partialRefund;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    List<SellerNextActionResponse> sellerNextActionResponse;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    List<ReturnLineItem> returnLineItems;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    List<DiscountAmount> discountAmount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    List<ShippingFeeAmount> shippingFeeAmount;



    @Transient
    public String getOrderId(){
        return order.getId();
    }
}
