package com.manchui.domain.repository;

import com.manchui.domain.entity.Gathering;
import com.manchui.domain.entity.Heart;
import com.manchui.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HeartRepository extends JpaRepository<Heart, Long> {

    Optional<Heart> findByUserAndGathering(User user, Gathering gathering);

}
