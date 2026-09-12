package com.example.auth.domain;

public record JwtResponse(String accessToken, String tokenType, long expiresInSeconds) {
    public static JwtResponse bearer(String token, long exp) {
        return new JwtResponse(token, "Bearer", exp);
    }
}
