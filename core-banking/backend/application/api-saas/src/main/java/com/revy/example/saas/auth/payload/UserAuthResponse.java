package com.revy.example.saas.auth.payload;

/** 관리자 인증 성공 시 클라이언트에 반환하는 토큰과 관리자 정보입니다. */
public record UserAuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType
) {
    public static UserAuthResponse of(String accessToken, String refreshToken) {
        return new UserAuthResponse(accessToken, refreshToken, "Bearer");
    }

}
