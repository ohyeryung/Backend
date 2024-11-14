package com.manchui.domain.service;

import com.manchui.domain.dto.CustomUserDetails;
import com.manchui.domain.dto.gathering.*;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface GatheringService {

    GatheringCreateResponse createGathering(String email, GatheringCreateRequest createRequest);

    GatheringCursorPagingResponse getGatherings(CustomUserDetails userDetails, Long cursor, int size, String query, String location, String startDate, String endDate, String category, String sort, boolean available);

    void joinGathering(String email, Long gatheringId);

    void joinCancelGathering(String username, Long gatheringId);

    void heartGathering(String email, Long gatheringId);

    void heartCancelGathering(String email, Long gatheringId);

    GatheringInfoResponse getGatheringInfoByGuest(Long gatheringId, Pageable pageable);

    GatheringInfoResponse getGatheringInfoByUser(String email, Long gatheringId, Pageable pageable);

    void cancelGathering(String email, Long gatheringId);

    GatheringPagingResponse getHeartList(String email, Pageable pageable, String query, String location, String startDate, String endDate, String category, String sort, boolean available);

}
