package com.manchui.domain.service;

import com.manchui.domain.dto.UserInfo;
import com.manchui.domain.entity.Gathering;

import java.util.List;

public interface GatheringReader {

    Gathering checkGathering(Long gatheringId);

    Gathering checkGatheringStatus(Long gatheringId);

    Gathering checkGatheringStatusIsClosed(Long gatheringId);

    List<UserInfo> getUserInfoList(Gathering gathering);

    Gathering checkGatheringStatusIsCanceled(Long gatheringId);

}
