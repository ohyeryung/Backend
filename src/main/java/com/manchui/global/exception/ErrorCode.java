package com.manchui.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    ILLEGAL_USERNAME_DUPLICATION(HttpStatus.CONFLICT, "중복된 이름 입니다."),
    ILLEGAL_EMAIL_DUPLICATION(HttpStatus.CONFLICT, "중복된 이메일 입니다."),
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "비밀번호는 영문자, 숫자, 특수문자 중 2가지 이상을 조합해야 합니다.");


    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {

        this.httpStatus = httpStatus;
        this.message = message;
    }

}
