package com.revy.api_server.domain.user.repo;

import com.revy.api_server.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
