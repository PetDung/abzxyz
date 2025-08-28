package com.petd.tiktok_system_be.exception;

import com.petd.tiktok_system_be.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;


@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  private static final String MIN_ATTRIBUTE = "min";

  @ExceptionHandler(value = Exception.class)
  public ResponseEntity<ApiResponse> handleGenericException(Exception  exception) {
    log.error("Unhandled exception occurred: ", exception);

    ApiResponse response = new ApiResponse();
    response.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
    response.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());

    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(value = AppException.class)
  public ResponseEntity<ApiResponse> handleAppException(AppException exception) {
    ApiResponse response = new ApiResponse();
    response.setCode(exception.getCode());
    response.setMessage(exception.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiResponse> handleMissingParams(MissingServletRequestParameterException ex) {
    ApiResponse response = new ApiResponse();
    response.setCode(400);
    response.setMessage("Thiếu tham số bắt buộc: " + ex.getParameterName());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(AuthorizationDeniedException.class)
  public ResponseEntity<ApiResponse> handleAuthorizationDenied(AuthorizationDeniedException ex) {
    ApiResponse response = ApiResponse.builder()
            .code(4003)
            .message("You do not have permission to perform this action")
            .build();
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
  }

  @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<ApiResponse> handleAccessDeniedException(AccessDeniedException exception) {
        ErrorCode errorCode = ErrorCode.ACCESS_DENIN;

        return ResponseEntity.status(errorCode.getStatusCode()).body(
            ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build()
        );
  }
//
//  @ExceptionHandler(value = MethodArgumentNotValidException.class)
//  public ResponseEntity<ApiResponse> handleValidationException(MethodArgumentNotValidException exception) {
//    FieldError fieldError = exception.getFieldError();
//    String enumKey = (fieldError != null) ? fieldError.getDefaultMessage() : null;
//
//    ErrorCode errorCode = ErrorCode.INVALID_KEY;
//    Map<String, Object> attributes = null;
//
//    if (enumKey != null) {
//      try {
//        errorCode = ErrorCode.valueOf(enumKey);
//
//        ObjectError objectError = exception.getBindingResult().getAllErrors().stream().findFirst().orElse(null);
//
//        if (objectError != null) {
//          try {
//            ConstraintViolation<?> constraintViolation = objectError.unwrap(ConstraintViolation.class);
//            attributes = constraintViolation.getConstraintDescriptor().getAttributes();
//            log.debug("Validation attributes: {}", attributes);
//          } catch (Exception e) {
//            log.warn("Unable to unwrap ConstraintViolation: {}", e.getMessage());
//          }
//        }
//
//      } catch (IllegalArgumentException e) {
//        log.warn("Invalid error code enumKey: {}", enumKey);
//      }
//    }
//
//    ApiResponse response = new ApiResponse();
//    response.setCode(errorCode.getCode());
//    response.setMessage(
//        (attributes != null)
//            ? mapAttribute(errorCode.getMessage(), attributes)
//            : errorCode.getMessage()
//    );
//
//    return ResponseEntity.status(errorCode.getStatusCode()).body(response);
//  }

  private String mapAttribute(String message, Map<String, Object> attributes) {
    String minValue = String.valueOf(attributes.getOrDefault(MIN_ATTRIBUTE, ""));
    return message.replace("{" + MIN_ATTRIBUTE + "}", minValue);
  }
}
