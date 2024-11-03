package com.manchui.domain.dto.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class WrittenReviewInfo {

    private Long gatheringId;
    private long score;
    private String groupName;
    private String category;
    private String location;
    private String gatheringImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
