package com.revy.example.admin;

import com.revy.example.admin.dto.AdminCredentialResult;
import com.revy.example.admin.dto.AdminResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface AdminReader {

    Optional<AdminResult> findById(Long id);

    Optional<AdminResult> findByEmail(String email);

    /** 인증 전용 — encodedPassword 포함. 로그인 흐름에서만 호출. */
    Optional<AdminCredentialResult> findCredentialByEmail(String email);

    boolean existsByEmail(String email);

    Page<AdminResult> search(Pageable pageable, String name);

    /** roles 포함하여 로드 (JWT 인증 흐름 전용). */
    Optional<AdminResult> findByIdWithRoles(Long id);
}
