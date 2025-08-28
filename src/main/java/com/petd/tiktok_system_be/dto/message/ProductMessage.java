package com.petd.tiktok_system_be.dto.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class ProductMessage {
    @JsonProperty("productId")
    String productId;
    @JsonProperty("shopId")
    String shopId;
    @JsonProperty("event")
    String event;
    @JsonProperty("update_time")
    Long updateTime;
}
