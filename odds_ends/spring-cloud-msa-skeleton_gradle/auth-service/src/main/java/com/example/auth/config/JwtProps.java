package com.example.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProps(String secret, String issuer, long expiresSeconds) {}
