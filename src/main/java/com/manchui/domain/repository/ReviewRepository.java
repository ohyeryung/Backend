package com.manchui.domain.repository;

import com.manchui.domain.entity.Gathering;
import com.manchui.domain.entity.Review;
import com.manchui.domain.entity.User;
import com.manchui.domain.repository.querydsl.ReviewQueryDsl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewQueryDsl {

    Optional<Review> findByGatheringAndUser(Gathering gathering, User user);

    List<Review> findByGathering(Gathering gathering);

    Optional<Review> findByIdAndDeletedAtIsNull(Long reviewId);

}
