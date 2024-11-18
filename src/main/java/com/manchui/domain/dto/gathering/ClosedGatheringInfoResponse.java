package com.manchui.domain.dto.gathering;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClosedGatheringInfoResponse {

    private Long gatheringId;

    private String groupName;

    private String category;

    private String location;

    private String gatheringImage;

    private String content;

    private int maxUsers;

    private int minUsers;

}
