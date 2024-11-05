package com.manchui.domain.dto.gathering;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatheringCreateResponse {

    private Long gatheringId;

    private String groupName;

    private String category;

    private String location;

    private String gatheringImage;

    private String gatheringContent;

    private LocalDateTime gatheringDate;

    private LocalDateTime dueDate;

    private int maxUsers;

    private int minUsers;

    private boolean isOpened;

    private boolean isCanceled;

    private boolean isClosed;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    private boolean isHearted;

}
