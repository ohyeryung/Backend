package com.manchui.domain.repository;

import com.manchui.domain.entity.Gathering;
import com.manchui.domain.entity.User;
import com.manchui.domain.repository.querydsl.GatheringCursorQueryDsl;
import com.manchui.domain.repository.querydsl.GatheringQueryDsl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GatheringRepository extends JpaRepository<Gathering, Long>, GatheringQueryDsl, GatheringCursorQueryDsl {

    Optional<Gathering> findByUserAndGroupName(User user, String groupName);

    Page<Gathering> findByUserEquals(User user, Pageable pageable);

    Page<Gathering> findByIdIn(List<Long> gatheringIdList, Pageable pageable);

}
