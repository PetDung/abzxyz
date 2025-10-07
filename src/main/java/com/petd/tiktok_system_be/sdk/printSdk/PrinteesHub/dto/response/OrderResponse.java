package com.petd.tiktok_system_be.sdk.printSdk.PrinteesHub.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderResponse {
    boolean status;
    String message;

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("order_fulfill_id")
    private String orderFulfillId;

    @JsonProperty("tracking_id")
    private String trackingId;

    private BigDecimal amount;

    private String originPrintStatus;
}
