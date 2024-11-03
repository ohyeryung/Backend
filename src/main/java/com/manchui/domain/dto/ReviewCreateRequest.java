package com.manchui.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewCreateRequest {

    @NotNull(message = "평점은 필수 입력 값입니다.")
    @Min(0)
    @Max(5)
    private int score;

    @NotNull(message = "내용은 필수 입력 값입니다.")
    private String comment;

}
