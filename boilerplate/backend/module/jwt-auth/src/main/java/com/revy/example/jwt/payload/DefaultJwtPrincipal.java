package com.revy.example.jwt.payload;

import com.revy.example.jwt.enums.JwtTokenType;
import com.revy.example.jwt.enums.PrincipalType;

/** JWT 인증 이후 SecurityContext에 저장되는 기본 인증 주체입니다. */
public record DefaultJwtPrincipal(
        Long id,
        String email,
        String role,
        PrincipalType principalType
) implements JwtPrincipal {
}
