package com.petd.tiktok_system_be.sdk.printSdk.PrinteesHub.dto.request;

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
    String designFront;
    String designBack;
    String mockupsFront;
    String mockupsBack;
}
