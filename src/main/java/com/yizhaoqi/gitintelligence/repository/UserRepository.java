package com.yizhaoqi.gitintelligence.repository;

import com.yizhaoqi.gitintelligence.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
