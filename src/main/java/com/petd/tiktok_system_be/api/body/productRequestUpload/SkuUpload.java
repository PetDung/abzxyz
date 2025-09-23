package com.petd.tiktok_system_be.api.body.productRequestUpload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SkuUpload {
    @JsonProperty("sales_attributes")
    private List<SalesAttribute> salesAttributes;
    private List<Inventory> inventory;
    @JsonProperty("seller_sku")
    private String sellerSku;
    private Price price;
    @JsonProperty("external_sku_id")
    private String externalSkuId;
    @JsonProperty("identifier_code")
    private IdentifierCode identifierCode;
    @JsonProperty("combined_skus")
    private List<CombinedSku> combinedSkus;
    @JsonProperty("sku_unit_count")
    private String skuUnitCount;
    @JsonProperty("external_urls")
    private List<String> externalUrls;
    @JsonProperty("extra_identifier_codes")
    private List<String> extraIdentifierCodes;
    @JsonProperty("pre_sale")
    private PreSale preSale;
    @JsonProperty("list_price")
    private Price listPrice;
    @JsonProperty("external_list_prices")
    private List<Price> externalListPrices;
}
