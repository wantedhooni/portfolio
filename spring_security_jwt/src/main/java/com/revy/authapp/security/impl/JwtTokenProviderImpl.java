package com.revy.authapp.security.impl;

import com.revy.authapp.domain.user.User;
import com.revy.authapp.security.JwtTokenProvider;
import com.revy.authapp.security.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProviderImpl implements JwtTokenProvider {
    private final JwtProperties jwtProperties;

    /**
     * 액세스 토큰을 생성한다.
     *
     * @param user 사용자 정보
     * @return 액세스 토큰
     */
    public String createAccessToken(User user) {
        return createToken(user.getId(), jwtProperties.accessTokenExpiration());
    }

    /**
     * 리프레시 토큰을 생성한다.
     *
     * @param user 사용자 정보
     * @return 리프레시 토큰
     */
    public String createRefreshToken(User user) {
        return createToken(user.getId(), jwtProperties.refreshTokenExpiration());
    }

    /**
     * 토큰에서 사용자 식별자를 추출한다.
     *
     * @param token 토큰 문자열
     * @return 사용자 식별자
     */
    public Long getUserId(String token) {
        return parseClaims(token).get("userId", Long.class);
    }

    /**
     * 토큰 만료 시간을 반환한다.
     *
     * @param token 토큰 문자열
     * @return 만료 시간
     */
    public Instant getExpiration(String token) {
        return parseClaims(token).getExpiration().toInstant();
    }

    /**
     * 토큰의 유효성을 검사한다.
     *
     * @param token 토큰 문자열
     * @return 유효 여부
     */
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
     * @param userId           사용자 식별자
     * @param expirationMillis 만료 시간(ms)
     * @return JWT 문자열
     */
    private String createToken(Long userId, long expirationMillis) {
        Instant now = Instant.now();
        return Jwts.builder()
                   .claim("userId", userId)
                   .issuedAt(Date.from(now))
                   .expiration(Date.from(now.plusMillis(expirationMillis)))
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
                   .build()
                   .parseSignedClaims(token)
                   .getPayload();
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
