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
public class OrderData {
    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("order_status")
    private String orderStatus;

    @JsonProperty("is_on_hold_order")
    private boolean isOnHoldOrder;

    @JsonProperty("update_time")
    private long updateTime;
}
