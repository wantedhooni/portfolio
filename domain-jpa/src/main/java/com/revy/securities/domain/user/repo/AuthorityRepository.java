package com.revy.securities.domain.user.repo;

import com.revy.securities.domain.user.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthorityRepository extends JpaRepository<Authority, String> {
    Optional<Authority> findByName(String userRead);
}
