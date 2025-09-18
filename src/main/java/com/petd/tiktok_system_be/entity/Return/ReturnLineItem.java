package com.petd.tiktok_system_be.entity.Return;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReturnLineItem {

    private String returnLineItemId;
    private String orderLineItemId;
    private String skuId;
    private String skuName;
    private String productName;
    private String sellerSku;
    private ProductImage productImage;
    private RefundAmount refundAmount;
}
