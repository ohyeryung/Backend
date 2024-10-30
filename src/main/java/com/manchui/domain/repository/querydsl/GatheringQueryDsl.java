package com.manchui.domain.repository.querydsl;

import com.manchui.domain.dto.GatheringListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GatheringQueryDsl {

    Page<GatheringListResponse> getGatheringListByGuest(Pageable pageable, String query, String location, String startDate, String endDate, String category);

    Page<GatheringListResponse> getGatheringListByUser(String email, Pageable pageable, String query, String location, String startDate, String endDate, String category);

}
