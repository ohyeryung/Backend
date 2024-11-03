package com.manchui.domain.service;

import com.manchui.domain.dto.review.ReviewInfo;
import com.manchui.domain.dto.UserInfo;
import com.manchui.domain.entity.Attendance;
import com.manchui.domain.entity.Gathering;
import com.manchui.domain.entity.Review;
import com.manchui.domain.repository.AttendanceRepository;
import com.manchui.domain.repository.GatheringRepository;
import com.manchui.domain.repository.ReviewRepository;
import com.manchui.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.manchui.global.exception.ErrorCode.*;

@Component
@RequiredArgsConstructor
public class GatheringReaderImpl implements GatheringReader {

    private final GatheringRepository gatheringRepository;

    private final AttendanceRepository attendanceRepository;

    private final ReviewRepository reviewRepository;

    @Override
    public Gathering checkGathering(Long gatheringId) {

        return gatheringRepository.findById(gatheringId).orElseThrow(
                () -> new CustomException(GATHERING_NOT_FOUND)
        );

    }

    @Override
    public Gathering checkGatheringStatus(Long gatheringId) {

        Gathering gathering = checkGathering(gatheringId);

        if (gathering.isCanceled()) {
            throw new CustomException(GATHERING_CANCELED);
        } else if (gathering.isClosed()) {
            throw new CustomException(GATHERING_CLOSED);
        }
        return gathering;
    }

    @Override
    public Gathering checkGatheringStatusClosed(Long gatheringId) {

        Gathering gathering = checkGathering(gatheringId);

        // 모임이 마감되지 않았거나 취소된 경우 예외 발생
        if (!gathering.isClosed() || gathering.isCanceled()) throw new CustomException(ILLEGAL_GATHERING_STATUS);

        return gathering;
    }

    @Override
    public List<UserInfo> getUserInfoList(Gathering gathering) {

        List<UserInfo> userInfoList = new ArrayList<>();
        for (Attendance attendance : attendanceRepository.findByGathering(gathering)) {
            if (attendance.getDeletedAt() == null)
                userInfoList.add(new UserInfo(attendance.getUser().getName(), attendance.getUser().getProfileImagePath()));
        }
        return userInfoList;
    }

    @Override
    public List<ReviewInfo> getReviewInfoList(Gathering gathering) {

        List<ReviewInfo> reviewInfoList = new ArrayList<>();
        for (Review review : reviewRepository.findByGathering(gathering)) {
            reviewInfoList.add(new ReviewInfo(review.getUser().getName(), review.getUser().getProfileImagePath(), review.getScore(), review.getComment(), review.getCreatedAt()));
        }

        return reviewInfoList;
    }

}
