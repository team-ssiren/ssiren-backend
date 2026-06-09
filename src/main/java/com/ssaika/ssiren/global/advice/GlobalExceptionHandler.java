package com.ssaika.ssiren.global.advice;

import com.ssaika.ssiren.global.dto.BaseResponse;
import com.ssaika.ssiren.global.exception.CustomException;
import com.ssaika.ssiren.global.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<BaseResponse<?>> handleCustomException(CustomException e) {
        log.error("CustomException: code={}, message={}", e.getErrorCode(), e.getMessage());

        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(BaseResponse.fail(
                errorCode,
                e.getMessage() != null ? e.getMessage() : errorCode.getMessage()
            ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<?>> handleValidationException(MethodArgumentNotValidException e) {
        // 첫 번째 에러 메시지만 추출 (단순화)
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(FieldError::getDefaultMessage)
            .orElse("입력값이 올바르지 않습니다.");

        log.error("Validation Error: {}", errorMessage);
        ErrorCode errorCode = ErrorCode.INVALID_PARAMETER;

        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(BaseResponse.fail(errorCode, errorMessage));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<BaseResponse<?>> handleNoResourceFoundException(NoResourceFoundException e) {
        ErrorCode errorCode = ErrorCode.NOT_FOUND;
        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(BaseResponse.fail(errorCode));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseResponse<?>> handleMethodArgumentTypeMismatchException(
        MethodArgumentTypeMismatchException e) {
        ErrorCode errorCode = ErrorCode.BAD_REQUEST;
        return ResponseEntity.status(errorCode.getHttpStatus())
            .body(BaseResponse.fail(errorCode));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse<?>> handleJsonParseException(
        HttpMessageNotReadableException e) {
        log.error("JSON Parse Error: {}", e.getMessage());

        ErrorCode errorCode = ErrorCode.INVALID_FORMAT;

        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(BaseResponse.fail(errorCode));
    }

    @ExceptionHandler(HttpMessageConversionException.class)
    public ResponseEntity<BaseResponse<?>> handleHttpMessageConversionException(
        HttpMessageConversionException e) {
        log.error("HTTP Message Conversion Error: {}", e.getMessage());

        ErrorCode errorCode = ErrorCode.INVALID_FORMAT;

        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(BaseResponse.fail(errorCode));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<BaseResponse<?>> handleHttpMediaTypeNotSupportedException(
        HttpMediaTypeNotSupportedException e) {
        log.error("Unsupported Media Type: {}", e.getMessage());

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(BaseResponse.fail(errorCode));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<?>> handleAllException(Exception e) {
        log.error("[UNEXPECTED ERROR]", e);

        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(errorCode.getHttpStatus())
            .body(BaseResponse.fail(errorCode));
    }

    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public void handleAsyncRequestTimeoutException(AsyncRequestTimeoutException e) {
        log.debug("Async request timed out. SSE connection may be closed.", e);
    }

    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleAsyncRequestNotUsableException(AsyncRequestNotUsableException e) {
        log.debug("Async request is no longer usable. client may be disconnected.", e);
    }
}
