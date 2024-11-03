package com.manchui.domain.dto.User;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
public class UserParticipatedGatheringResponse {

    private long gatheringCount;
    private Page<GatheringInfo> participatedGatheringList;
    private long pageSize;
    private long page;
    private long totalPage;

    public UserParticipatedGatheringResponse(long gatheringCount, Page<GatheringInfo> participatedGatheringList, long pageSize, long page, long totalPage) {
        this.gatheringCount = gatheringCount;
        this.participatedGatheringList = participatedGatheringList;
        this.pageSize = pageSize;
        this.page = page;
        this.totalPage = totalPage;
    }
}
