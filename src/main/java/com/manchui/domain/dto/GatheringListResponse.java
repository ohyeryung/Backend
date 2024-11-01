package com.manchui.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class GatheringListResponse {

    private String name;

    private String profileImage;

    private Long gatheringId;

    private String groupName;

    private String category;

    private String location;

    private String gatheringImage;

    private LocalDateTime gatheringDate;

    private LocalDateTime dueDate;

    private int maxUsers;

    private int minUsers;

    private Long currentUsers;

    private boolean isOpened;

    private boolean isClosed;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    private boolean isHearted;

}
