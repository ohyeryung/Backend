package com.manchui.domain.repository;

import com.manchui.domain.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Image findByGatheringId(Long gatheringId);
}
