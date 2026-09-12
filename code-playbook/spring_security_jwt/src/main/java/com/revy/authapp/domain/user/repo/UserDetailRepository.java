package com.revy.authapp.domain.user.repo;

import com.revy.authapp.domain.user.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDetailRepository extends JpaRepository<UserDetail, Long> {
}
