package com.petd.tiktok_system_be.api.body.productRequestUpload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductAttribute {
    private String id;
    private List<Value> values;
}
