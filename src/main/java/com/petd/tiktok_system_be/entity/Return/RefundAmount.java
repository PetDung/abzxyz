package com.petd.tiktok_system_be.entity.Return;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RefundAmount {

    private String currency;
    private BigDecimal refundTotal;
    private BigDecimal refundSubtotal;
    private BigDecimal refundShippingFee;
    private BigDecimal refundTax;
    private BigDecimal retailDeliveryFee;
    private BigDecimal buyerServiceFee;
}
