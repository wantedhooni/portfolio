package com.revy.example.saas.auth.usecase;

import com.revy.example.jwt.payload.LoginRequest;
import com.revy.example.jwt.payload.LogoutRequest;
import com.revy.example.jwt.payload.RefreshTokenRequest;
import com.revy.example.saas.auth.payload.SignupRequest;
import com.revy.example.saas.auth.payload.SignupResponse;
import com.revy.example.saas.auth.payload.UserAuthResponse;
import jakarta.validation.Valid;

public interface AuthUseCase {
    UserAuthResponse login(@Valid LoginRequest request);

    UserAuthResponse refresh(@Valid RefreshTokenRequest request);

    void logout(String authorization, LogoutRequest request);

    /** 신규 사용자를 가입시키고 접수 정보를 반환합니다. */
    SignupResponse signup(@Valid SignupRequest request);
}
