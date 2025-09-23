package com.petd.tiktok_system_be.sdk.printSdk.PrinteesHub.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductOption {
    String sku;
    int quantity;

    @JsonProperty("designFront")
    String designFront;

    @JsonProperty("designBack")
    String designBack;

    @JsonProperty("mockupsFront")
    String mockupsFront;

    @JsonProperty("mockupsBack")
    String mockupsBack;
}
