package com.petd.tiktok_system_be.exception;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppException extends RuntimeException {

  private String message;
  private int code;

  public AppException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.message = errorCode.getMessage();
    this.code  = errorCode.getCode();
  }
  public AppException(int code, String message) {
    super(message);
    this.message = message;
    this.code  = code;
  }
}
