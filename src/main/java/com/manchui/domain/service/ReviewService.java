package com.manchui.domain.service;

import com.manchui.domain.dto.ReviewCreateResponse;
import org.springframework.stereotype.Service;

@Service
public interface ReviewService {

    ReviewCreateResponse createReview(String email, Long gatheringId, ReviewCreateResponse createResponse);

}
