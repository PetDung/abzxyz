package com.petd.tiktok_system_be.dto.message;

import com.petd.tiktok_system_be.api.body.Event;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class WebhookMessage {
    Event event;
    String shopId;
}
