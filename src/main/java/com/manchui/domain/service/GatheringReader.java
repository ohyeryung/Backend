package com.manchui.domain.service;

import com.manchui.domain.dto.UserInfo;
import com.manchui.domain.entity.Gathering;
import com.manchui.domain.entity.User;

import java.util.List;
import java.util.Optional;

public interface GatheringReader {

    Optional<Gathering> findGathering(User user, String groupName);

    Gathering checkGathering(Long gatheringId);

    Gathering checkGatheringStatus(Long gatheringId);

    Gathering checkGatheringStatusIsClosed(Long gatheringId);

    List<UserInfo> getUserInfoList(Gathering gathering);

    Gathering checkGatheringStatusIsCanceled(Long gatheringId);

    List<Gathering> findClosedGathering(User user);

}
