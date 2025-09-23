package com.petd.tiktok_system_be.sdk.printSdk.PrinteesHub.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PrintOrderRequest {
    String orderId;
    List<OrderItemPrintRequest> itemIds;
}
