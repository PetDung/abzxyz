package com.petd.tiktok_system_be.sdk.printSdk.Monkeykingprint.dto.request;

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
public class OrderRequestMKP {
    @JsonProperty("orderData")
    private OrderData orderData;
}
