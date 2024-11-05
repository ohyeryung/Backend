package com.manchui.domain.repository.querydsl;

import com.manchui.domain.dto.review.ReviewDetailInfo;
import com.manchui.domain.dto.review.ReviewInfo;
import com.manchui.domain.dto.review.ReviewScoreInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewQueryDsl {

    ReviewScoreInfo getScoreStatisticsByGathering(Long gatheringId);

    ReviewScoreInfo getScoreStatistics(String query, String location, String category, String startDate, String endDate, int score);

    Page<ReviewInfo> getReviewInfoList(Pageable pageable, Long gatheringId);

    Page<ReviewDetailInfo> getReviewDetailInfo(Pageable pageable, String query, String location, String startDate, String endDate, String category, String sort, int score);

}
