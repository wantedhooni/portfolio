package com.revy.example.admin.api.auth.usecase;

import com.revy.example.admin.api.auth.payload.AdminAuthResponse;
import com.revy.example.jwt.payload.LoginRequest;
import com.revy.example.jwt.payload.LogoutRequest;
import com.revy.example.jwt.payload.RefreshTokenRequest;
import jakarta.validation.Valid;

public interface AuthUseCase {
    AdminAuthResponse login(@Valid LoginRequest request);

    AdminAuthResponse refresh(@Valid RefreshTokenRequest request);

    void logout(String authorization, LogoutRequest request);
}
