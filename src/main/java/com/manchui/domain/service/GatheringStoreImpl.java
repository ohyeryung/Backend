package com.manchui.domain.service;

import com.manchui.domain.dto.GatheringCreateRequest;
import com.manchui.domain.entity.Attendance;
import com.manchui.domain.entity.Gathering;
import com.manchui.domain.entity.User;
import com.manchui.domain.repository.AttendanceRepository;
import com.manchui.domain.repository.GatheringRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class GatheringStoreImpl implements GatheringStore {

    private final GatheringRepository gatheringRepository;

    private final AttendanceRepository attendanceRepository;

    @Override
    @Transactional
    public Gathering saveGathering(GatheringCreateRequest createRequest, User user, LocalDateTime gatheringDate, LocalDateTime dueDate) {

        return gatheringRepository.save(createRequest.toRegisterEntity(user, gatheringDate, dueDate));
    }

    @Override
    @Transactional
    public void saveAttendance(User user, Gathering gathering) {

        attendanceRepository.save(Attendance.builder()
                .user(user)
                .gathering(gathering)
                .build());
    }

}
