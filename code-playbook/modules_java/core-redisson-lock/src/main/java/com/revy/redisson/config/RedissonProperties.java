package com.revy.redisson.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redisson 단일 서버 접속 속성을 보관한다.
 *
 * @param host     Redis 호스트
 * @param port     Redis 포트
 * @param db       Redis 데이터베이스 번호
 * @param password Redis 비밀번호
 */
@ConfigurationProperties(prefix = "redisson")
public record RedissonProperties(Boolean enabled,
                                 String host,
                                 Integer port,
                                 Integer db,
                                 String password) {

}
