package com.manchui.global.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SuccessResponse<T> {

    private boolean success;
    private String message;
    private T data;

    private static <T> SuccessResponse<T> success(T data, String message) {
        return SuccessResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> SuccessResponse<T> successWithData(T data) {
        return success(data, "OK");
    }

    public static <T> SuccessResponse<T> successWithNoData(String message) {
        return success(null, message);
    }

}
