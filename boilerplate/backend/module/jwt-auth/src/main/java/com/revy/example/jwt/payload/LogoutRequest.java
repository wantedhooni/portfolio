package com.revy.example.jwt.payload;

/** 로그아웃 시 함께 폐기할 리프레시 토큰 값을 담는 공통 DTO입니다. */
public record LogoutRequest(
        String refreshToken
) {
}
