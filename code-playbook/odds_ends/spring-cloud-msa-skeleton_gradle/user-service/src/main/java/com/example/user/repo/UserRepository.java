package com.example.user.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.user.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);
}
