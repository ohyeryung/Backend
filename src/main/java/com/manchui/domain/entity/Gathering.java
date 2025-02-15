package com.manchui.domain.entity;

import com.manchui.domain.dto.gathering.ClosedGatheringInfoResponse;
import com.manchui.domain.dto.gathering.GatheringCreateResponse;
import com.manchui.global.entity.Timestamped;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Table(name = "gathering")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Gathering extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gathering_id", nullable = false)
    @Comment("모임 id")
    private Long id;

    @Column(name = "group_name", nullable = false)
    @Size(min = 3, max = 30)
    @Comment("모임 이름")
    private String groupName;

    @Column(name = "location", nullable = false)
    @Comment("모임 장소")
    private String location;

    @Column(name = "category", nullable = false)
    @Comment("모임 카테고리")
    private String category;

    @Column(name = "gathering_content", nullable = false)
    @Comment("모임 상세 내용")
    private String gatheringContent;

    @Column(name = "gathering_date", nullable = false)
    @Comment("모임 날짜")
    private LocalDateTime gatheringDate;

    @Column(name = "due_date", nullable = false)
    @Comment("모집 마감 날짜")
    private LocalDateTime dueDate;

    @Column(name = "max_users")
    @Min(2)
    @Max(100)
    @Comment("모집 정원")
    private int maxUsers = 20;

    @Column(name = "min_users")
    @Min(2)
    @Max(100)
    @Comment("모집 최소 인원")
    private int minUsers = 2;

    @Column(name = "is_opened")
    @Comment("개설여부")
    @Builder.Default
    private boolean isOpened = false;

    @Column(name = "is_canceled")
    @Comment("취소여부")
    @Builder.Default
    private boolean isCanceled = false;

    @Column(name = "is_closed")
    @Comment("마감여부")
    @Builder.Default
    private boolean isClosed = false;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @Comment("회원 id")
    private User user;

    public GatheringCreateResponse toResponseDto(String filePath) {

        return GatheringCreateResponse.builder()
                .gatheringId(id)
                .groupName(groupName)
                .category(category)
                .location(location)
                .gatheringImage(filePath)
                .gatheringContent(gatheringContent)
                .gatheringDate(gatheringDate)
                .dueDate(dueDate)
                .maxUsers(maxUsers)
                .minUsers(minUsers)
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .isOpened(isOpened)
                .isCanceled(isCanceled)
                .isClosed(isClosed)
                .build();

    }

    public void cancel() {

        this.isCanceled = true;
    }

    // 모임 최소 인원 충족 시 개설 확정으로 상태값 변경
    public void open() {

        this.isOpened = true;
    }

    // 모임 최소 인원 미충족 시 개설 확정 false 로 상태값 변경
    public void close() {

        this.isOpened = false;
    }

    public void reopen(LocalDateTime gatheringDate, LocalDateTime dueDate, String location, int maxUsers, int minUsers, String gatheringContent) {

        this.isOpened = false;
        this.isClosed = false;
        this.gatheringDate = gatheringDate;
        this.dueDate = dueDate;
        this.location = location;
        this.maxUsers = maxUsers;
        this.minUsers = minUsers;
        this.gatheringContent = gatheringContent;

    }

    public ClosedGatheringInfoResponse toClosedResponseDto(String gatheringImage) {

        return ClosedGatheringInfoResponse.builder()
                .gatheringId(id)
                .groupName(groupName)
                .category(category)
                .location(location)
                .gatheringImage(gatheringImage)
                .content(getGatheringContent())
                .maxUsers(maxUsers)
                .minUsers(minUsers)
                .build();
    }

}
