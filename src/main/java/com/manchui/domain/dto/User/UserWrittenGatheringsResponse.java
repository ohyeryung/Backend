package com.manchui.domain.dto.User;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
public class UserWrittenGatheringsResponse {

    private long gatheringCount;
    private Page<GatheringInfo> writtenGatheringList;
    private long pageSize;
    private long page;
    private long totalPage;

    public UserWrittenGatheringsResponse(long gatheringCount, Page<GatheringInfo> writtenGatheringList, long pageSize, long page, long totalPage) {
        this.gatheringCount = gatheringCount;
        this.writtenGatheringList = writtenGatheringList;
        this.pageSize = pageSize;
        this.page = page;
        this.totalPage = totalPage;
    }
}
