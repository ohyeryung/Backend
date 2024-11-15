package com.manchui.domain.dto.gathering;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ClosedGatheringResponse {

    private int gatheringCount;

    private List<ClosedGathering> closedGatheringList;

}
