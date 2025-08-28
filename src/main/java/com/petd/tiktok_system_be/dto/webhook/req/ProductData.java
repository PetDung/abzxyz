package com.petd.tiktok_system_be.dto.webhook.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductData {

    @JsonProperty("product_id")
    String productId;
    @JsonProperty("status")
    String status;
    @JsonProperty("suspended_reason")
    String suspendedReason;
    @JsonProperty("update_time")
    long updateTime;
}
