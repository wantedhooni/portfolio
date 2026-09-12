package com.revy.api_server.application.infra.security.token;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis 기반 토큰 저장소 구현체.
 */

public interface TokenStore {
    /**
     * 리프레시 토큰을 저장한다.
     *
     * @param userId 사용자 식별자
     * @param token 리프레시 토큰
     * @param ttl 만료 시간
     */
    void saveRefreshToken(Long userId, String token, Duration ttl);

    /**
     * 저장된 리프레시 토큰을 조회한다.
     *
     * @param userId 사용자 식별자
     * @return 리프레시 토큰
     */
    Optional<String> findRefreshToken(Long userId);

    /**
     * 리프레시 토큰을 삭제한다.
     *
     * @param userId 사용자 식별자
     */
    void deleteRefreshToken(Long userId);

    /**
     * 로그아웃된 액세스 토큰을 블랙리스트에 등록한다.
     *
     * @param token 액세스 토큰
     * @param ttl 만료 시간
     */
    void blacklistAccessToken(String token, Duration ttl);

    /**
     * 블랙리스트에 등록된 액세스 토큰인지 확인한다.
     *
     * @param token 액세스 토큰
     * @return 블랙리스트 여부
     */
    boolean isAccessTokenBlacklisted(String token);
}
