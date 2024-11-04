package com.manchui.domain.controller;

import com.manchui.domain.dto.CustomUserDetails;
import com.manchui.domain.dto.review.ReviewCreateRequest;
import com.manchui.domain.dto.review.ReviewCreateResponse;
import com.manchui.domain.service.ReviewService;
import com.manchui.global.response.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/{gatheringId}")
    public ResponseEntity<SuccessResponse<ReviewCreateResponse>> createReview(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long gatheringId,
                                                                              @Valid @RequestBody ReviewCreateRequest createRequest) {

        return ResponseEntity.status(201).body(SuccessResponse.successWithData(reviewService.createReview(userDetails.getUsername(), gatheringId, createRequest)));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<SuccessResponse<ReviewCreateResponse>> updateReview(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long reviewId,
                                                                              @Valid @RequestBody ReviewCreateRequest updateRequest) {

        return ResponseEntity.ok().body(SuccessResponse.successWithData(reviewService.updateReview(userDetails.getUsername(), reviewId, updateRequest)));
    }


}
