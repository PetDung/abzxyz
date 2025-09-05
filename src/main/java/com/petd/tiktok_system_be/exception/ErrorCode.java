package com.petd.tiktok_system_be.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {

  UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),

  USER_NOT_FOUND(4004, "Không tìm thấy tài khoản", HttpStatus.NOT_FOUND),
  SHOP_NOT_FOUND(4004, "Không tìm thấy shop", HttpStatus.CONFLICT),

  NOT_FOUND(4004, "Không tìm thấy đối tượng", HttpStatus.NOT_FOUND),

  FI(4003, "Bạn không có quyền trên hành động này", HttpStatus.CONFLICT),
  ACCESS_DENIN(4003, "Bạn không có quyền trên hành động này", HttpStatus.FORBIDDEN),
  EXIST_AL(4009, "Đối tượng đã tồn tại", HttpStatus.CONFLICT),
  RQ(4009, "Thiếu dữ liệu", HttpStatus.CONFLICT),
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
