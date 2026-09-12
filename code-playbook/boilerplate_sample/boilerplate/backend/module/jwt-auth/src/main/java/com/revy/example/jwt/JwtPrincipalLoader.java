package com.revy.example.jwt;

import com.revy.example.jwt.enums.PrincipalType;
import com.revy.example.jwt.payload.JwtPrincipal;

import java.util.Optional;

/** JWT subject 값을 실제 인증 주체로 복원하는 도메인 어댑터 계약입니다. */
public interface JwtPrincipalLoader {

    PrincipalType supports();

    Optional<JwtPrincipal> load(Long principalId);
}
