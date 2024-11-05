package com.manchui.domain.dto.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ReviewableGatheringInfo {

    private Long gatheringId;
    private String groupName;
    private String category;
    private String location;
    private String gatheringImage;
    private LocalDateTime gatheringDate;
    private long maxUsers;
    private long participantUsers;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
