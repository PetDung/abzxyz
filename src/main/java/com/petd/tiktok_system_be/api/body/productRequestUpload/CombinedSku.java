package com.petd.tiktok_system_be.api.body.productRequestUpload;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CombinedSku {
    @JsonProperty("product_id")
    private String productId;
    @JsonProperty("sku_id")
    private String skuId;
    @JsonProperty("sku_count")
    private Integer skuCount;

}
