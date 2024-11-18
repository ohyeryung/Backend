package com.manchui.domain.dto.gathering;

import com.manchui.domain.dto.UserInfo;
import com.manchui.domain.dto.review.ReviewDetailPagingResponse;
import com.manchui.domain.entity.Gathering;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class GatheringInfoResponse {

    private Long gatheringId;

    private String name;

    private String profileImagePath;

    private String groupName;

    private String category;

    private String location;

    private String gatheringImage;

    private String content;

    private LocalDateTime gatheringDate;

    private LocalDateTime dueDate;

    private int maxUsers;

    private int minUsers;

    private int currentUsers;

    private int heartCounts;

    private boolean isOpened;

    private boolean isCanceled;

    private boolean isClosed;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    private boolean isHearted;

    private List<UserInfo> usersList;

    private ReviewDetailPagingResponse reviewsList;

    public GatheringInfoResponse(Gathering gathering, String filePath, int currentUsers, int heartCounts, boolean isHearted, List<UserInfo> userInfoList, ReviewDetailPagingResponse reviewsList) {

        this.gatheringId = gathering.getId();
        this.name = gathering.getUser().getName();
        this.profileImagePath = gathering.getUser().getProfileImagePath();
        this.groupName = gathering.getGroupName();
        this.category = gathering.getCategory();
        this.location = gathering.getLocation();
        this.gatheringImage = filePath;
        this.content = gathering.getGatheringContent();
        this.gatheringDate = gathering.getGatheringDate();
        this.dueDate = gathering.getDueDate();
        this.maxUsers = gathering.getMaxUsers();
        this.minUsers = gathering.getMinUsers();
        this.currentUsers = currentUsers;
        this.heartCounts = heartCounts;
        this.isOpened = gathering.isOpened();
        this.isCanceled = gathering.isCanceled();
        this.isClosed = gathering.isClosed();
        this.createdAt = gathering.getCreatedAt();
        this.updatedAt = gathering.getUpdatedAt();
        this.deletedAt = gathering.getDeletedAt();
        this.isHearted = isHearted;
        this.usersList = userInfoList;
        this.reviewsList = reviewsList;
    }

}
