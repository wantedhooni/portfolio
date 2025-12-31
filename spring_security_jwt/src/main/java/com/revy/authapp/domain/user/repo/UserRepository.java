package com.revy.authapp.domain.user.repo;

import com.revy.authapp.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
