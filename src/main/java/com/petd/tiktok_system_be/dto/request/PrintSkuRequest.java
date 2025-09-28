package com.petd.tiktok_system_be.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PrintSkuRequest {
    String skuCode;
    String type;
    String value1;
    String value2;
}
