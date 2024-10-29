package com.manchui.domain.service;

import com.manchui.domain.dto.GatheringCreateRequest;
import com.manchui.domain.dto.GatheringCreateResponse;
import com.manchui.domain.dto.GatheringPagingResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface GatheringService {

    GatheringCreateResponse createGathering(String email, GatheringCreateRequest createRequest);

    GatheringPagingResponse getGatheringByGuest(Pageable pageable, String query, String location, String date);

    GatheringPagingResponse getGatheringByUser(String email, Pageable pageable, String query, String location, String date);

    void joinGathering(String email, Long gatheringId);

}
