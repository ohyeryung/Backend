package com.manchui.global.exception.handler;

import com.manchui.global.exception.CustomException;
import com.manchui.global.exception.ErrorCode;
import com.manchui.global.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.format.DateTimeParseException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {

        log.error("handleHttpRequestMethodNotSupportedException", ex);

        final ErrorResponse response
                = ErrorResponse
                .create()
                .httpStatus(HttpStatus.METHOD_NOT_ALLOWED)
                .message(ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(value = {IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {

        log.error("handleIllegalArgumentException : {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.create()
                .message(ex.getMessage())
                .httpStatus(HttpStatus.BAD_REQUEST);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(value = {NullPointerException.class})
    public ResponseEntity<ErrorResponse> handleNullPointException(NullPointerException ex) {

        log.error("handleNullPointException : {}", ex.getMessage());

        ex.printStackTrace();

        ErrorResponse response = ErrorResponse.create()
                .message(ex.getMessage())
                .httpStatus(HttpStatus.BAD_REQUEST);

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * @Vaild 검증 실패 시 에러 처리
     */
    @ExceptionHandler(value = {BindException.class})
    protected ResponseEntity<ErrorResponse> handleBindException(BindException ex) {

        log.error("handleBindException : {}", ex.getMessage());

        String message = ex.getMessage();

        String defaultMsg = message.substring(message.lastIndexOf("[") + 1, message.lastIndexOf("]") - 1); // "[" 또는 "]" 기준으로 메시지 추출
        log.error("에러 메세지 : {}", defaultMsg);

        ErrorResponse response = ErrorResponse.create()
                .message(defaultMsg)
                .httpStatus(HttpStatus.BAD_REQUEST);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(value = {CustomException.class})
    protected ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {

        log.error("handleCustomException", ex);

        ErrorCode errorCode = ex.getErrorCode();
        String message = ex.getMessage();

        ErrorResponse response
                = ErrorResponse
                .create()
                .message(message)
                .httpStatus(errorCode.getHttpStatus());

        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    @ExceptionHandler(value = {DateTimeParseException.class})
    protected ResponseEntity<ErrorResponse> handleDateTimeParseException(DateTimeParseException ex) {

        log.error("handleDateTimeParseException", ex);

        ErrorResponse response = ErrorResponse.create()
                .message(ex.getMessage())
                .httpStatus(HttpStatus.BAD_REQUEST);

        return ResponseEntity.badRequest().body(response);
    }

}
