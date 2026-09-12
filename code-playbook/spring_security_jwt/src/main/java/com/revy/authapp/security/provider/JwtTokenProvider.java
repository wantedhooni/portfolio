package com.revy.authapp.security.provider;

import com.revy.authapp.domain.user.User;

import java.time.temporal.Temporal;

public interface JwtTokenProvider {
    boolean validateToken(String token);

    Long getUserId(String token);

    String createAccessToken(User user);

    String createRefreshToken(User user);

    Temporal getExpiration(String refreshToken);
}

