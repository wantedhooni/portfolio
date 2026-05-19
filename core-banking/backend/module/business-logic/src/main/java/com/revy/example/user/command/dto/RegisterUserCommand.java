package com.revy.example.user.command.dto;

/**
 * 비밀번호는 호출부(application layer)에서 PasswordEncoder로 인코딩 후 전달.
 * business-logic은 Spring Security 의존성 미주입.
 */
public record RegisterUserCommand(
        String email,
        String encodedPassword,
        String name
) {}
