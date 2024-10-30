package com.manchui.domain.repository;

import com.manchui.domain.entity.Heart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HeartRepository extends JpaRepository<Heart, Long> {

    Optional<Heart> findByUserAndGathering(UUID userId, Long gatheringId);

}
