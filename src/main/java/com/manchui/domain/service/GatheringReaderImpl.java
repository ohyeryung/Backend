package com.manchui.domain.service;

import com.manchui.domain.dto.UserInfo;
import com.manchui.domain.entity.Attendance;
import com.manchui.domain.entity.Gathering;
import com.manchui.domain.repository.AttendanceRepository;
import com.manchui.domain.repository.GatheringRepository;
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
    public List<UserInfo> getUserInfoList(Gathering gathering) {

        List<UserInfo> userInfoList = new ArrayList<>();
        for (Attendance attendance : attendanceRepository.findByGathering(gathering)) {
            if (attendance.getDeletedAt() == null)
                userInfoList.add(new UserInfo(attendance.getUser().getName(), attendance.getUser().getProfileImagePath()));
        }
        return userInfoList;
    }

}
