package com.manchui.domain.dto.User;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
public class UserWrittenGatheringsResponse {

    private long gatheringCount;
    private Page<WrittenGathering> writtenGatheringList;
    private long pageSize;
    private long page;
    private long totalPage;

    public UserWrittenGatheringsResponse(long gatheringCount, Page<WrittenGathering> writtenGatheringList, long pageSize, long page, long totalPage) {
        this.gatheringCount = gatheringCount;
        this.writtenGatheringList = writtenGatheringList;
        this.pageSize = pageSize;
        this.page = page;
        this.totalPage = totalPage;
    }
}
