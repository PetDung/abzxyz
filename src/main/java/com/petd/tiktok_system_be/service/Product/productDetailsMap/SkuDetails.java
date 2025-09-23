package com.petd.tiktok_system_be.service.Product.productDetailsMap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.petd.tiktok_system_be.api.body.productRequestUpload.CombinedSku;
import com.petd.tiktok_system_be.api.body.productRequestUpload.IdentifierCode;
import com.petd.tiktok_system_be.api.body.productRequestUpload.Inventory;
import com.petd.tiktok_system_be.api.body.productRequestUpload.PreSale;
import com.petd.tiktok_system_be.api.body.productRequestUpload.Price;
import com.petd.tiktok_system_be.api.body.productRequestUpload.SalesAttribute;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SkuDetails {
    private List<SalesAttribute> salesAttributes;
    private List<Inventory> inventory;
    private String sellerSku;
    private Price price;
    private String externalSkuId;
    private IdentifierCode identifierCode;
    private List<CombinedSku> combinedSkus;
    private String skuUnitCount;
    private List<String> externalUrls;
    private List<String> extraIdentifierCodes;
    private PreSale preSale;
    private Price listPrice;
    private List<Price> externalListPrices;
} 