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
public class ShippingFeeAmount {

    private String currency;
    private BigDecimal sellerPaidReturnShippingFee;
    private BigDecimal platformPaidReturnShippingFee;
    private BigDecimal buyerPaidReturnShippingFee;
}
