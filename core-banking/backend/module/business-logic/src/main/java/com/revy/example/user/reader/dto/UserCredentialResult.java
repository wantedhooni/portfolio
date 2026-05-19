package com.revy.example.user.reader.dto;

import com.revy.example.domain.user.User;

/**
 * 인증 전용 — encodedPassword 포함.
 * 로그인 흐름에서 PasswordEncoder.matches() 호출 직후 폐기.
 */
public record UserCredentialResult(
        Long id,
        String email,
        String encodedPassword,
        String role
) {

    public static UserCredentialResult from(User user) {
        return new UserCredentialResult(user.getId(), user.getEmail(), user.getPassword(), user.getRole());
    }
}
