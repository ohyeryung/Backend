package com.manchui.domain.dto.gathering;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class GatheringPagingResponse {

    private int gatheringCount;
    private List<?> gatheringList = new ArrayList<>();
    private int pageSize;
    private int page;
    private int totalPage;

    public GatheringPagingResponse(Page<?> pageList) {

        this.gatheringCount = (int) pageList.getTotalElements();
        this.gatheringList = pageList.getContent();
        this.pageSize = pageList.getSize();
        this.page = pageList.getPageable().getPageNumber();
        this.totalPage = pageList.getTotalPages();

    }

}
