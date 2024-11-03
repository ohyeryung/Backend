package com.manchui.domain.repository.querydsl;

import com.manchui.domain.dto.ReviewInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewQueryDsl {

    Page<ReviewInfo> getReviewInfoList(Pageable pageable, Long gatheringId);

}
