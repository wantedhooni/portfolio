package com.revy.example.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.PostgreSQLContainer

// TestcontainersConfig.kt
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfig {

    @Bean
    @ServiceConnection                                  // Spring Boot 4 자동 DataSource 연결
    fun postgresContainer(): PostgreSQLContainer<*> =
        PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("shop_test")
            .withReuse(true)                            // 컨테이너 재사용으로 빌드 속도 향상
}