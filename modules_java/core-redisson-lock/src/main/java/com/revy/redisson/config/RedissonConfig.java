package com.revy.redisson.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 단일 Redis 서버에 연결하는 Redisson 클라이언트를 구성한다.
 */
@EnableTransactionManagement(
        order = Ordered.LOWEST_PRECEDENCE
)
@Configuration
@EnableConfigurationProperties({RedissonProperties.class})
@ConditionalOnBooleanProperty(prefix = "redisson", name = "enabled")
public class RedissonConfig {

    /**
     * 분산 락에서 사용할 Redisson 클라이언트를 생성한다.
     *
     * @param redissonProperties Redis 접속 설정
     * @return Redisson 클라이언트
     */
    @Bean
    public RedissonClient redissonClient(RedissonProperties redissonProperties) {
        Config config = new Config();

        config.useSingleServer()
              .setAddress("redis://" + redissonProperties.host() + ":" + redissonProperties.port())
              .setDatabase(redissonProperties.db());


        if (redissonProperties.password() != null && !redissonProperties.password().isBlank()) {
            config.useSingleServer().setPassword(redissonProperties.password());
        }

        return Redisson.create(config);
    }
}
