package com.manchui.domain.controller;

import com.manchui.domain.dto.CustomUserDetails;
import com.manchui.domain.dto.review.ReviewCreateRequest;
import com.manchui.domain.dto.review.ReviewCreateResponse;
import com.manchui.domain.dto.review.ReviewDetailPagingResponse;
import com.manchui.domain.service.ReviewService;
import com.manchui.global.response.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<SuccessResponse<ReviewCreateResponse>> deleteReview(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long reviewId) {

        reviewService.deleteReview(userDetails.getUsername(), reviewId);
        return ResponseEntity.ok().body(SuccessResponse.successWithNoData("후기가 정상적으로 삭제되었습니다."));
    }

    @GetMapping("")
    public ResponseEntity<SuccessResponse<ReviewDetailPagingResponse>> searchReview(@RequestParam(defaultValue = "1") int page,
                                                                                    @RequestParam int size,
                                                                                    @RequestParam(required = false) String query,
                                                                                    @RequestParam(required = false) String location,
                                                                                    @RequestParam(required = false) String startDate,
                                                                                    @RequestParam(required = false) String endDate,
                                                                                    @RequestParam(required = false) String category,
                                                                                    @RequestParam(required = false) String sort) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(SuccessResponse.successWithData(reviewService.searchReview(pageable, query, location, startDate, endDate, category, sort)));
    }

}
