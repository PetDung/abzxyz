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
public class Price {
    private String amount;
    private String currency;
    @JsonProperty("sale_price")
    private String salePrice;
    private String source;
}
