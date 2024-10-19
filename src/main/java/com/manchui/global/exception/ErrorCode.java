package com.manchui.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    ILLEGAL_USERNAME_DUPLICATION(HttpStatus.CONFLICT, "중복된 이름 입니다."),
    ILLEGAL_EMAIL_DUPLICATION(HttpStatus.CONFLICT, "중복된 이메일 입니다.");


    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {

        this.httpStatus = httpStatus;
        this.message = message;
    }

}
