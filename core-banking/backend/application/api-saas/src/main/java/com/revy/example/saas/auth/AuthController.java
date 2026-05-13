package com.revy.example.saas.auth;

import com.revy.example.core.common.ApiResponse;
import com.revy.example.jwt.payload.JwtPrincipal;
import com.revy.example.jwt.payload.LoginRequest;
import com.revy.example.jwt.payload.LogoutRequest;
import com.revy.example.jwt.payload.RefreshTokenRequest;
import com.revy.example.saas.auth.payload.UserAuthResponse;
import com.revy.example.saas.auth.usecase.AuthUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
class AuthController {

    private final AuthUseCase authUseCase;
    public AuthController(AuthUseCase authUseCase) {
        this.authUseCase = authUseCase;
    }

    @PostMapping("/login")
    public ApiResponse<UserAuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authUseCase.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<UserAuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.ok(authUseCase.refresh(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestBody(required = false) LogoutRequest request
    ) {
        authUseCase.logout(authorization, request);
        return ApiResponse.ok();
    }

    @GetMapping("/me")
    public ApiResponse<JwtPrincipal> me(@AuthenticationPrincipal JwtPrincipal principal) {
        return ApiResponse.ok(principal);
    }
}
