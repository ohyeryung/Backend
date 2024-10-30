package com.manchui.domain.dto;

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

    private String image;

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

    private boolean isOpened;

    private boolean isCanceled;

    private boolean isClosed;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    private boolean isHearted;

    private List<UserInfo> usersList;

    private List<ReviewInfo> reviewList;

    public GatheringInfoResponse(Gathering gathering, String gatheringImage, int currentUsers, List<UserInfo> userInfoList, List<ReviewInfo> reviewInfoList) {

        this.gatheringId = gathering.getId();
        this.name = gathering.getUser().getName();
        this.image = gathering.getUser().getProfileImagePath();
        this.groupName = gathering.getGroupName();
        this.category = gathering.getCategory();
        this.location = gathering.getLocation();
        this.gatheringImage = gatheringImage;
        this.content = gathering.getGatheringContent();
        this.gatheringDate = gathering.getGatheringDate();
        this.dueDate = gathering.getDueDate();
        this.maxUsers = gathering.getMaxUsers();
        this.minUsers = gathering.getMinUsers();
        this.currentUsers = currentUsers;
        this.isOpened = gathering.isOpened();
        this.isCanceled = gathering.isCanceled();
        this.isClosed = gathering.isClosed();
        this.createdAt = gathering.getCreatedAt();
        this.updatedAt = gathering.getUpdatedAt();
        this.deletedAt = gathering.getDeletedAt();
        this.isHearted = gathering.isHearted();
        this.usersList = userInfoList;
        this.reviewList = reviewInfoList;
    }

    // 회원
    public GatheringInfoResponse(Gathering gathering, String gatheringImage, int currentUsers, boolean isHearted, List<UserInfo> userInfoList, List<ReviewInfo> reviewInfoList) {

        this.gatheringId = gathering.getId();
        this.name = gathering.getUser().getName();
        this.image = gathering.getUser().getProfileImagePath();
        this.groupName = gathering.getGroupName();
        this.category = gathering.getCategory();
        this.location = gathering.getLocation();
        this.gatheringImage = gatheringImage;
        this.content = gathering.getGatheringContent();
        this.gatheringDate = gathering.getGatheringDate();
        this.dueDate = gathering.getDueDate();
        this.maxUsers = gathering.getMaxUsers();
        this.minUsers = gathering.getMinUsers();
        this.currentUsers = currentUsers;
        this.isOpened = gathering.isOpened();
        this.isCanceled = gathering.isCanceled();
        this.isClosed = gathering.isClosed();
        this.createdAt = gathering.getCreatedAt();
        this.updatedAt = gathering.getUpdatedAt();
        this.deletedAt = gathering.getDeletedAt();
        this.isHearted = isHearted;
        this.usersList = userInfoList;
        this.reviewList = reviewInfoList;

    }

}
