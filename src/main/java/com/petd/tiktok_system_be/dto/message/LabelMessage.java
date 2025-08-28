package com.petd.tiktok_system_be.dto.message;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LabelMessage {
    String orderId;
    String trackingNumber;
    String label;
}
