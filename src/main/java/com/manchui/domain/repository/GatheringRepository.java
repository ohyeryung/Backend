package com.manchui.domain.repository;

import com.manchui.domain.entity.Gathering;
import com.manchui.domain.entity.User;
import com.manchui.domain.repository.querydsl.GatheringQueryDsl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatheringRepository extends JpaRepository<Gathering, Long>, GatheringQueryDsl {

    Page<Gathering> findByUserEquals(User user, Pageable pageable);
}
