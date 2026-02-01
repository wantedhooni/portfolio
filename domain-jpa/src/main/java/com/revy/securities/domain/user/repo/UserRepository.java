package com.revy.securities.domain.user.repo;

import com.revy.securities.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
