package com.manchui.domain.dto.User;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GatheringInfo {
    private Long gatheringId;
    private String groupName;
    private String category;
    private String location;
    private String gatheringImage;
    private LocalDateTime gatheringDate;
    private LocalDateTime dueDate;
    private int maxUsers;
    private int participantUsers;
    private Boolean isOpened;
    private Boolean isCanceled;
    private Boolean isClosed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public GatheringInfo(Long gatheringId, String groupName, String category, String location, String gatheringImage
                            , LocalDateTime gatheringDate, LocalDateTime dueDate, int maxUsers, int participantUsers
                            , Boolean isOpened, Boolean isCanceled, Boolean isClosed, LocalDateTime createdAt
                            , LocalDateTime updatedAt, LocalDateTime deletedAt) {
        this.gatheringId = gatheringId;
        this.groupName = groupName;
        this.category = category;
        this.location = location;
        this.gatheringImage = gatheringImage;
        this.gatheringDate = gatheringDate;
        this.dueDate = dueDate;
        this.maxUsers = maxUsers;
        this.participantUsers = participantUsers;
        this.isOpened = isOpened;
        this.isCanceled = isCanceled;
        this.isClosed = isClosed;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }
}
