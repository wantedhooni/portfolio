package com.example.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.auth.domain.JwtResponse;
import com.example.auth.domain.LoginRequest;
import com.example.auth.service.AuthService;
import com.example.common.api.ApiResponse;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/token")
    public ApiResponse<JwtResponse> token(@RequestBody LoginRequest req) {
        String token = authService.issueToken(req.username(), req.password());
        return ApiResponse.ok(JwtResponse.bearer(token, authService.expiresSeconds()));
    }
}
