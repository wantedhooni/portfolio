package com.revy.example.admin.dto;

import com.revy.example.domain.admin.Admin;

/**
 * 인증 전용 — encodedPassword 포함.
 * 로그인 흐름에서 PasswordEncoder.matches() 호출 직후 폐기.
 */
public record AdminCredentialResult(
        Long id,
        String email,
        String encodedPassword,
        String role
) {

    public static AdminCredentialResult from(Admin admin) {
        return new AdminCredentialResult(admin.getId(), admin.getEmail(), admin.getPassword(), admin.getRole());
    }
}
