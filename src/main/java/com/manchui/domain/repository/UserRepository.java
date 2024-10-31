package com.manchui.domain.repository;

import com.manchui.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Boolean existsByName(String name);

    Boolean existsByEmail(String email);

    User findByEmail(String email);
}
