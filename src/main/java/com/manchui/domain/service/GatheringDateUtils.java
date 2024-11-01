package com.manchui.domain.service;

import com.manchui.global.exception.CustomException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.manchui.global.exception.ErrorCode.ILLEGAL_DUE_DATE;
import static com.manchui.global.exception.ErrorCode.ILLEGAL_GATHERING_DATE;

@Component
@RequiredArgsConstructor
public class GatheringDateUtils {

    @Value("${gathering.date-pattern}")
    private String gatheringDatePattern;
    private DateTimeFormatter formatter;
    @Value("${gathering.due-date-hours-before}")
    private int dueDateHoursBeforeGathering;

    @PostConstruct
    private void init() {
        // gatheringDatePattern이 주입된 후에 초기화
        formatter = DateTimeFormatter.ofPattern(gatheringDatePattern);
    }

    // 모임 날짜 변환
    public LocalDateTime parseGatheringDate(String gatheringDateStr) {

        return LocalDateTime.parse(gatheringDateStr, formatter);
    }

    // 마감일자 계산
    public LocalDateTime calculateDueDateFromGatheringDate(LocalDateTime gatheringDate) {

        return gatheringDate.minusHours(dueDateHoursBeforeGathering);
    }

    // 모임 생성 시 날짜 검증
    public void checkGatheringDates(LocalDateTime gatheringDate, LocalDateTime dueDate) {

        LocalDateTime now = LocalDateTime.now();

        if (!gatheringDate.isAfter(now.plusDays(1))) {
            throw new CustomException(ILLEGAL_GATHERING_DATE);
        }
        if (!dueDate.isAfter(now)) {
            throw new CustomException(ILLEGAL_DUE_DATE);
        }
    }

}
