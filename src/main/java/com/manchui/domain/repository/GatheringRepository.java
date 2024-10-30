package com.manchui.domain.repository;

import com.manchui.domain.entity.Gathering;
import com.manchui.domain.repository.querydsl.GatheringQueryDsl;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatheringRepository extends JpaRepository<Gathering, Long>, GatheringQueryDsl {
}
