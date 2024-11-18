package com.manchui.domain.service;

import com.manchui.domain.dto.UserInfo;
import com.manchui.domain.entity.Attendance;
import com.manchui.domain.entity.Gathering;
import com.manchui.domain.entity.User;
import com.manchui.domain.repository.AttendanceRepository;
import com.manchui.domain.repository.GatheringRepository;
import com.manchui.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.manchui.global.exception.ErrorCode.*;

@Component
@RequiredArgsConstructor
public class GatheringReaderImpl implements GatheringReader {

    private final GatheringRepository gatheringRepository;

    private final AttendanceRepository attendanceRepository;

    @Override
    public Optional<Gathering> findGathering(User user, String groupName) {

        return gatheringRepository.findByUserAndGroupName(user, groupName);
    }

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
    public Gathering checkGatheringStatusIsClosed(Long gatheringId) {

        Gathering gathering = checkGathering(gatheringId);

        // 모임이 마감되지 않았거나 취소된 모임인 경우, 모임 일자가 현재 시점으로부터 지나지 않은 경우 예외 발생
        if (!gathering.isClosed() || gathering.isCanceled() || !gathering.getGatheringDate().isBefore(LocalDateTime.now()))
            throw new CustomException(ILLEGAL_GATHERING_STATUS);

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
    public Gathering checkGatheringStatusIsCanceled(Long gatheringId) {

        Gathering gathering = checkGathering(gatheringId);

        // 모임이 취소된 경우 예외 발생
        if (gathering.isCanceled()) {
            throw new CustomException(GATHERING_CANCELED);
        }

        return gathering;
    }

    @Override
    public List<Gathering> findClosedGathering(User user) {

        // 유저가 생성한 모임 중 마감되었으면서 취소되지 않고 실제 모임 날짜도 지난 상태의 모임만 조회
        List<Gathering> gatherings = gatheringRepository.findByUserAndIsClosedAndIsCanceled(user, true, false);

        // 추가 조건으로 필터링
        return gatherings.stream()
                .filter(gathering -> gathering.getGatheringDate().isBefore(LocalDateTime.now()))
                .toList();
    }


}
