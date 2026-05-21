package com.revy.example.jwt.payload;

import com.revy.example.jwt.enums.PrincipalType;

import java.util.Set;

/** JWT 인증 모듈이 도메인 엔티티와 무관하게 다루는 인증 주체 계약입니다. */
public interface JwtPrincipal {

    Long id();

    String email();

    String role();

    PrincipalType principalType();

    /** RBAC 세분 권한 목록. Spring Security authorities로 등록됩니다. */
    Set<String> permissions();
}
