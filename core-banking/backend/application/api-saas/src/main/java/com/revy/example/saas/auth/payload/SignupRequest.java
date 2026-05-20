package com.revy.example.saas.auth.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 신규 사용자 가입 요청 페이로드입니다. */
public record SignupRequest(
        @NotBlank @Size(max = 50) String name,
        @NotBlank @Email         String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank @Size(max = 100) String organizationName,
        @NotBlank @Size(max = 10)  String currency,
        @NotBlank                  String accountType
) {}
