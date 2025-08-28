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

public class OrderDetalisSyncMessage {

    @JsonProperty("shopId")
    String shopId;

    @JsonProperty("orderId")
    String orderId;

    @JsonProperty("limit")
    int limit;

}
