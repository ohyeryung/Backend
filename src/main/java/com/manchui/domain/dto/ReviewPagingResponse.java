package com.manchui.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class ReviewPagingResponse {

    private int reviewCount;
    private List<?> reviewList = new ArrayList<>();
    private int pageSize;
    private int page;
    private int totalPage;

    public ReviewPagingResponse(Page<?> pageList) {

        this.reviewCount = (int) pageList.getTotalElements();
        this.reviewList = pageList.getContent();
        this.pageSize = pageList.getSize();
        this.page = pageList.getPageable().getPageNumber();
        this.totalPage = pageList.getTotalPages();

    }

}
