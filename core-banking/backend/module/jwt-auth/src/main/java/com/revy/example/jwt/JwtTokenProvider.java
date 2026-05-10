package com.revy.example.jwt;

import com.revy.example.jwt.enums.JwtTokenType;
import com.revy.example.jwt.payload.JwtPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/** JWT 액세스 토큰의 생성과 파싱을 담당하는 모듈 서비스입니다. */
@Component
public class JwtTokenProvider {



    private final JwtProperties properties;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(JwtPrincipal principal) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(properties.accessTokenExpirationMinutes() * 60);
        return createToken(principal, JwtTokenType.ACCESS, now, expiresAt);
    }

    public String createRefreshToken(JwtPrincipal principal) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(properties.refreshTokenExpirationDays() * 24 * 60 * 60);
        return createToken(principal, JwtTokenType.REFRESH, now, expiresAt);
    }

    private String createToken(JwtPrincipal principal, JwtTokenType tokenType, Instant issuedAt, Instant expiresAt) {
        return Jwts.builder()
                .issuer(properties.issuer())
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(principal.id()))
                .claim(JwtConstants.PRINCIPAL_TYPE_CLAIM, principal.principalType())
                .claim(JwtConstants.TOKEN_TYPE_CLAIM, tokenType.name())
                .claim(JwtConstants.EMAIL_CLAIM, principal.email())
                .claim(JwtConstants.ROLE_CLAIM, principal.role())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(properties.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public JwtTokenType getTokenType(Claims claims) {
        return JwtTokenType.valueOf(claims.get(JwtConstants.TOKEN_TYPE_CLAIM, String.class));
    }

    public String getTokenId(Claims claims) {
        return claims.getId();
    }

    public Instant getExpiration(Claims claims) {
        return claims.getExpiration().toInstant();
    }
}
