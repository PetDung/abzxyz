package com.petd.tiktok_system_be.sdk.exception;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TiktokException extends RuntimeException {
    int code;
    String message;
}
