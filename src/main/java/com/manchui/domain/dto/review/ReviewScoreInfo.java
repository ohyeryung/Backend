package com.manchui.domain.dto.review;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewScoreInfo {

    private double avgScore;
    private long fiveScoreCount;
    private long fourScoreCount;
    private long threeScoreCount;
    private long twoScoreCount;
    private long oneScoreCount;

}
