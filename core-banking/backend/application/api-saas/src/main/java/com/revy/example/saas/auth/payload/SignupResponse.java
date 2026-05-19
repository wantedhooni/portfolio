package com.revy.example.saas.auth.payload;

import java.time.Instant;
import java.util.UUID;

/** 신규 사용자 가입 성공 응답입니다. */
public record SignupResponse(
        String requestId,
        String email,
        String organizationName,
        String status,
        String createdAt
) {
    public static SignupResponse of(String email, String organizationName) {
        return new SignupResponse(
                UUID.randomUUID().toString(),
                email,
                organizationName,
                "RECEIVED",
                Instant.now().toString()
        );
    }
}
