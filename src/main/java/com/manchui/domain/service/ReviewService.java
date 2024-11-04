package com.manchui.domain.service;

import com.manchui.domain.dto.review.ReviewCreateRequest;
import com.manchui.domain.dto.review.ReviewCreateResponse;
import org.springframework.stereotype.Service;

@Service
public interface ReviewService {

    ReviewCreateResponse createReview(String email, Long gatheringId, ReviewCreateRequest createResponse);

    ReviewCreateResponse updateReview(String email, Long reviewId, ReviewCreateRequest updateRequest);

    void deleteReview(String email, Long reviewId);

}
