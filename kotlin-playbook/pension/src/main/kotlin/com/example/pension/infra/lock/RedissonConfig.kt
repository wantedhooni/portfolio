package com.example.pension.infra.lock

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RedissonConfig {

    @Bean(destroyMethod = "shutdown")
    fun redissonClient(
        @Value("\${app.redis.address}") address: String,
        @Value("\${app.redis.password:}") password: String,
    ): RedissonClient {
        val config = Config()

        val single = config.useSingleServer()
            .setAddress(address)
            .setConnectionMinimumIdleSize(4)
            .setConnectionPoolSize(32)
            .setTimeout(3_000)

        if (password.isNotBlank()) {
            single.password = password
        }

        // leaseTime 미지정 lock에서 watchdog이 갱신하는 기본 timeout.
        config.lockWatchdogTimeout = 30_000

        return Redisson.create(config)
    }
}