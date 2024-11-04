package com.manchui.domain.repository.querydsl;

import com.manchui.domain.dto.review.ReviewInfo;
import com.manchui.domain.dto.review.ReviewScoreInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewQueryDsl {

    ReviewScoreInfo getScoreStatistics(Long gatheringId);

    Page<ReviewInfo> getReviewInfoList(Pageable pageable, Long gatheringId);

}
