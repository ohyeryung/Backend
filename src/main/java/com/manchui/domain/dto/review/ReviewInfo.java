package com.manchui.domain.dto.review;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ReviewInfo {

    private String name;
    private String profileImagePath;
    private int score;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
