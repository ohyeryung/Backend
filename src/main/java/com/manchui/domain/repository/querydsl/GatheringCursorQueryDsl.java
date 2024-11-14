package com.manchui.domain.repository.querydsl;

import com.manchui.domain.dto.gathering.GatheringCursorPagingResponse;

public interface GatheringCursorQueryDsl {

    // 비회원 모임 목록 조회 (커서 기반 페이징)
    GatheringCursorPagingResponse getGatheringListByGuest(Long cursor, int size, String query, String location, String startDate, String endDate, String category, String sort, boolean available);

    // 회원 모임 목록 조회 (커서 기반 페이징)
    GatheringCursorPagingResponse getGatheringListByUser(String email, Long cursor, int size, String query, String location, String startDate, String endDate, String category, String sort, boolean available);

}
