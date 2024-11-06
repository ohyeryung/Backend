package com.manchui.domain.dto.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
@AllArgsConstructor
public class UserReviewableGatheringsResponse {

    private long reviewableCount;
    private Page<ReviewableGatheringInfo> reviewableList;
    private long pageSize;
    private long page;
    private long totalPage;
}
