package com.manchui.domain.dto.review;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ReviewDetailInfo {

    private Long gatheringId;

    private String groupName;

    private String gatheringImage;

    private String category;

    private String location;

    private String name;

    private String profileImagePath;

    private Long reviewId;

    private int score;

    private String comment;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
