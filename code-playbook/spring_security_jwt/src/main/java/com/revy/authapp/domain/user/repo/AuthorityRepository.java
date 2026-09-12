package com.revy.authapp.domain.user.repo;

import com.revy.authapp.domain.user.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthorityRepository extends JpaRepository<Authority, String> {
    Optional<Authority> findByName(String userRead);
}
