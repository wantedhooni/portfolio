package com.revy.example.admin.dto;

import com.revy.example.domain.admin.Admin;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 인증 전용 — encodedPassword 포함.
 * 로그인 흐름에서 PasswordEncoder.matches() 호출 직후 폐기.
 */
public record AdminCredentialResult(
        Long id,
        String email,
        String encodedPassword,
        String role,
        Set<String> permissions
) {

    public static AdminCredentialResult from(Admin admin) {
        Set<String> perms = admin.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(Enum::name)
                .collect(Collectors.toSet());
        return new AdminCredentialResult(admin.getId(), admin.getEmail(), admin.getPassword(), admin.getRole(), perms);
    }
}
