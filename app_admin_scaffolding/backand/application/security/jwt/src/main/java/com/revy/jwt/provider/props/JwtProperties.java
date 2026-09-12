package com.revy.jwt.provider.props;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public record JwtProperties(
        @Value("${app.security.jwt.secret}")
        String secret,
        @Value("${app.security.jwt.issuer}")
        String issuer,
        @Value("${app.security.jwt.accessTokenExpirationMin}")
        long accessTokenExpirationMin,
        @Value("${app.security.jwt.refreshTokenExpirationMin}")
        long refreshTokenExpirationMin

) {
}
