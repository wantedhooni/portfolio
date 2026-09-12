package com.revy.securities.domain.user.repo;

import com.revy.securities.domain.user.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDetailRepository extends JpaRepository<UserDetail, Long> {
}
