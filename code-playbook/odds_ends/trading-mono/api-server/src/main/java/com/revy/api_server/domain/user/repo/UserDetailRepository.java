package com.revy.api_server.domain.user.repo;

import com.revy.api_server.domain.user.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDetailRepository extends JpaRepository<UserDetail, Long> {
}
