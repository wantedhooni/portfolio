package com.revy.example.jwt;

import com.revy.example.jwt.enums.JwtTokenType;

/** JWT 인증 이후 SecurityContext에 저장되는 기본 인증 주체입니다. */
public record DefaultJwtPrincipal(
        Long id,
        String email,
        String role,
        JwtTokenType principalType
) implements JwtPrincipal {
}
