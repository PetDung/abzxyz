package com.petd.tiktok_system_be.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {

  UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
  USER_NOT_FOUND(4004, "Không tìm thấy tài khoản", HttpStatus.NOT_FOUND),
  FI(4004, "Bạn không có quyền trên hành động này", HttpStatus.CONFLICT),
  ;

  private final int code;
  private final String message;
  private final HttpStatusCode statusCode;
  ErrorCode(int code, String message, HttpStatusCode statusCode) {
    this.code = code;
    this.message = message;
    this.statusCode = statusCode;
  }
}
