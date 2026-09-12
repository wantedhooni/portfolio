package com.revy.api_server.application.infra.security.provider;

import com.revy.securities.domain.user.User;

import java.time.temporal.Temporal;

public interface JwtTokenProvider {

    boolean validateToken(String token);

    Long getUserId(String token);

    String createAccessToken(User user);

    String createRefreshToken(User user);

    Temporal getExpiration(String refreshToken);
}
