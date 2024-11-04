package com.manchui.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // user
    ILLEGAL_USERNAME_DUPLICATION(HttpStatus.CONFLICT, "중복된 이름 입니다."),
    ILLEGAL_EMAIL_DUPLICATION(HttpStatus.CONFLICT, "중복된 이메일 입니다."),
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "비밀번호는 영문자, 숫자, 특수문자 중 2가지 이상을 조합해야 합니다."),
    MISSING_EMAIL(HttpStatus.BAD_REQUEST, "아이디를 입력해주세요."),
    MISSING_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호를 입력해주세요."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 계정입니다."),

    // token
    MISSING_AUTHORIZATION_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "유효한 Refresh 토큰이 요청에 포함되지 않았습니다."),
    MISSING_AUTHORIZATION_ACCESS_TOKEN(HttpStatus.BAD_REQUEST, "유효한 Access 토큰이 요청에 포함되지 않았습니다."),
    EXPIRED_JWT(HttpStatus.UNAUTHORIZED, "만료된 토큰 입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 Refresh 토큰입니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 Access 토큰입니다."),

    // gathering
    ILLEGAL_MIN_USERS(HttpStatus.BAD_REQUEST, "최소 인원은 최대 인원보다 클 수 없습니다."),
    ILLEGAL_DUE_DATE(HttpStatus.BAD_REQUEST, "마감 기한은 최소 하루 이상이어야 합니다."),
    ILLEGAL_GATHERING_DATE(HttpStatus.BAD_REQUEST, "모임 날짜는 현재 시점으로부터 최소 24시간 이후여야 합니다."),
    GATHERING_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 모임입니다."),
    GATHERING_CANCELED(HttpStatus.BAD_REQUEST, "취소된 모임입니다."),
    GATHERING_CLOSED(HttpStatus.BAD_REQUEST, "모집이 마감된 모임입니다."),
    ALREADY_JOIN_GATHERING(HttpStatus.BAD_REQUEST, "이미 참여 신청된 모임입니다."),
    ALREADY_HEART_GATHERING(HttpStatus.BAD_REQUEST, "이미 좋아요를 누른 모임입니다."),
    ATTENDANCE_NOT_EXIST(HttpStatus.BAD_REQUEST, "참여한 모임만 취소/후기 등록이 가능합니다."),
    UNAUTHORIZED_GATHERING_CANCEL(HttpStatus.BAD_REQUEST, "본인이 생성한 모임만 취소 가능합니다."),
    GATHERING_FULL(HttpStatus.BAD_REQUEST, "모집 정원이 다 찬 경우에는 참여할 수 없습니다."),
    MUST_JOIN_IN(HttpStatus.BAD_REQUEST, "모임 주최자는 필수 참석입니다"),

    // review
    ILLEGAL_GATHERING_STATUS(HttpStatus.BAD_REQUEST, "마감된 모임만 후기 등록이 가능합니다."),
    ALREADY_REVIEW_EXIST(HttpStatus.BAD_REQUEST, "참여한 모임에 대한 후기는 1회만 등록 가능합니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 후기입니다."),

    // image
    ILLEGAL_EMPTY_FILE(HttpStatus.BAD_REQUEST, "이미지 파일은 필수 입력 값입니다."),
    WRONG_TYPE_IMAGE(HttpStatus.BAD_REQUEST, "유효하지 않은 이미지 파일형식 입니다."),
    FAILED_UPLOAD_IMAGE(HttpStatus.BAD_REQUEST, "이미지 업로드에 실패했습니다."),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "이미지를 찾을수 없습니다.");


    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {

        this.httpStatus = httpStatus;
        this.message = message;
    }

}
