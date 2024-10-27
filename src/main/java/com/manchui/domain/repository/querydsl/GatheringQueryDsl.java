package com.manchui.domain.repository.querydsl;

import com.manchui.domain.dto.GatheringListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GatheringQueryDsl {

    Page<GatheringListResponse> getGatheringListByGuest(Pageable pageable, String query, String location, String date);

}
