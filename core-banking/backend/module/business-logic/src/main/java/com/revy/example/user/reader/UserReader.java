package com.revy.example.user.reader;

import com.revy.example.user.reader.dto.UserCredentialResult;
import com.revy.example.user.reader.dto.UserResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserReader {

    Optional<UserResult> findById(Long id);

    Optional<UserResult> findByEmail(String email);

    /** 인증 전용 — encodedPassword 포함. 로그인 흐름에서만 호출. */
    Optional<UserCredentialResult> findCredentialByEmail(String email);

    boolean existsByEmail(String email);

    Page<UserResult> search(Pageable pageable, String name);
}
