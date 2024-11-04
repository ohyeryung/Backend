package com.manchui.domain.dto.review;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor
public class ReviewDetailPagingResponse {

    private int reviewCount;

    private ReviewScoreInfo scoreList;

    private List<?> reviewContentList;

    private int pageSize;

    private int page;

    private int totalPage;

    public ReviewDetailPagingResponse(Page<?> pageList, ReviewScoreInfo scoreInfo) {

        this.reviewCount = (int) pageList.getTotalElements();
        this.scoreList = scoreInfo;
        this.reviewContentList = pageList.getContent();
        this.pageSize = pageList.getSize();
        this.page = pageList.getPageable().getPageNumber();
        this.totalPage = pageList.getTotalPages();
    }

}
