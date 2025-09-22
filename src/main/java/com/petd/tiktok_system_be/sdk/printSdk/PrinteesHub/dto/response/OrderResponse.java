package com.petd.tiktok_system_be.sdk.printSdk.PrinteesHub.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    boolean status;
    String message;
    String orderId;
    String orderFulfillId;
    String trackingId;
    double amount;
}
