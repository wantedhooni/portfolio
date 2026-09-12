package com.revy.api_server.application.infra.security.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProp(
        String secret,
        String issuer,
        long accessTokenExpirationMin,
        long refreshTokenExpirationMin

) {
}
