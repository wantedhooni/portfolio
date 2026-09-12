package com.revy.jwt.provider.impl;

import com.revy.jwt.provider.JwtTokenProvider;
import com.revy.jwt.provider.props.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProviderImpl implements JwtTokenProvider {
        private final JwtProperties jwtProperties;

    @Override
    public String createAccessToken(String userId) {
        log.debug("Creating access token for user {}", userId);
        return createToken(userId, jwtProperties.accessTokenExpirationMin());
    }

    @Override
    public String createRefreshToken(String userId) {
        log.debug("Creating refresh token for user {}", userId);
        return createToken(userId, jwtProperties.refreshTokenExpirationMin());
    }

    /**
     * 토큰에서 사용자 식별자를 추출한다.
     *
     * @param token 토큰 문자열
     * @return 사용자 식별자
     */
    @Override
    public String getUserId(String token) {
        return parseClaims(token).get("userId", String.class);
    }

    /**
     * 토큰 만료 시간을 반환한다.
     *
     * @param token 토큰 문자열
     * @return 만료 시간
     */
    @Override
    public Instant getExpiration(String token) {
        return parseClaims(token).getExpiration().toInstant();
    }

    /**
     * 토큰의 유효성을 검사한다.
     *
     * @param token 토큰 문자열
     * @return 유효 여부
     */
    @Override
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 사용자 식별자와 만료 시간을 기반으로 토큰을 생성한다.
     *
     * @param userId        사용자 식별자
     * @param expirationMin 만료 시간(분)
     * @return JWT 문자열
     */
    private String createToken(String userId, long expirationMin) {
        Instant now = Instant.now();
        return Jwts.builder()
                   .claim("userId", userId)
                   .issuedAt(Date.from(now))
                   .expiration(Date.from(now.plusSeconds(expirationMin * 60)))
                   .signWith(getSigningKey())
                   .compact();
    }

    /**
     * 토큰 클레임을 파싱한다.
     *
     * @param token 토큰 문자열
     * @return 클레임
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                   .verifyWith(getSigningKey())
                   .build().parseSignedClaims(token).getPayload();
    }

    /**
     * 서명에 사용할 키를 생성한다.
     *
     * @return 서명 키
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.secret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
