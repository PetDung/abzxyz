package com.petd.tiktok_system_be.sdk.printSdk.PrinteesHub.dto.exception;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PrintersHubApiException extends RuntimeException {
    boolean status;
    String message;
}
