package com.revy.api_server.application.infra.security.token;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisTokenStore implements TokenStore {
    private static final String REFRESH_PREFIX = "refresh:";
    private static final String LOGOUT_PREFIX = "logout:";

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveRefreshToken(Long userId, String token, Duration ttl) {
        redisTemplate.opsForValue().set(REFRESH_PREFIX + userId, token, ttl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> findRefreshToken(Long userId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(REFRESH_PREFIX + userId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete(REFRESH_PREFIX + userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void blacklistAccessToken(String token, Duration ttl) {
        redisTemplate.opsForValue().set(LOGOUT_PREFIX + token, "logout", ttl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAccessTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(LOGOUT_PREFIX + token));
    }
}
