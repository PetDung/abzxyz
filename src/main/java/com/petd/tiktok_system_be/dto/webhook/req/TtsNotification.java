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
public class TtsNotification<T> {

    private int type;

    @JsonProperty("tts_notification_id")
    private String ttsNotificationId;

    @JsonProperty("shop_id")
    private String shopId;

    private long timestamp;

    private T data;
}
