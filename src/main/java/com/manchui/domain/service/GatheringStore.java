package com.manchui.domain.service;

import com.manchui.domain.dto.GatheringCreateRequest;
import com.manchui.domain.entity.Gathering;
import com.manchui.domain.entity.User;

import java.time.LocalDateTime;

public interface GatheringStore {

    Gathering saveGathering(GatheringCreateRequest createRequest, User user, LocalDateTime gatheringDate, LocalDateTime dueDate);

    void saveAttendance(User user, Gathering gathering);

}
