package com.manchui.domain.service;

import com.manchui.domain.dto.GatheringCreateRequest;
import com.manchui.domain.dto.GatheringCreateResponse;
import org.springframework.stereotype.Service;

@Service
public interface GatheringService {

    GatheringCreateResponse createGathering(String email, GatheringCreateRequest createRequest);

}
