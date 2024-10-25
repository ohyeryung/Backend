package com.manchui.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    ILLEGAL_USERNAME_DUPLICATION(HttpStatus.CONFLICT, "중복된 이름 입니다."),
    ILLEGAL_EMAIL_DUPLICATION(HttpStatus.CONFLICT, "중복된 이메일 입니다."),
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "비밀번호는 영문자, 숫자, 특수문자 중 2가지 이상을 조합해야 합니다."),
    MISSING_AUTHORIZATION_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "유효한 Refresh 토큰이 요청에 포함되지 않았습니다."),
    EXPIRED_JWT(HttpStatus.UNAUTHORIZED,"만료된 토큰 입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 Refresh 토큰입니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 Access 토큰입니다."),
    MISSING_EMAIL(HttpStatus.BAD_REQUEST, "아이디를 입력해주세요."),
    MISSING_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호를 입력해주세요.");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {

        this.httpStatus = httpStatus;
        this.message = message;
    }

}
