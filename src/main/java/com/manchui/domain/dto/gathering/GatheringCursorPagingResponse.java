package com.manchui.domain.dto.gathering;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GatheringCursorPagingResponse {

    private int gatheringCount;

    private List<GatheringListResponse> gatheringList;

    private Long nextCursor;

    public GatheringCursorPagingResponse(List<GatheringListResponse> gatheringList, int gatheringCount) {

        this.gatheringList = gatheringList;
        this.gatheringCount = gatheringCount;
        this.nextCursor = gatheringList.isEmpty() ? null : gatheringList.get(gatheringList.size() - 1).getGatheringId();
    }

}
