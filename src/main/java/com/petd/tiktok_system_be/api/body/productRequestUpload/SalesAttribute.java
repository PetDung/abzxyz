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
public class SalesAttribute {
    private String id;
    @JsonProperty("value_id")
    private String valueId;
    @JsonProperty("value_name")
    private String valueName;
    @JsonProperty("sku_img")
    private Image skuImg;
    private String name;
    @JsonProperty("supplementary_sku_images")
    private List<Image> supplementarySkuImages;
}
