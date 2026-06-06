package com.revy.example.common.retry;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Spring Retry 활성화 설정.
 *
 * <p>{@code com.revy} 하위를 component-scan 하는 모든 애플리케이션
 * (api-admin / api-saas / server-executor)에서 자동으로 적용된다.</p>
 *
 * <p>{@code @EnableRetry} 의 기본 advisor 순서가 트랜잭션 advisor 보다 앞서므로
 * {@code @Retryable} 이 트랜잭션을 바깥에서 감싸 매 재시도마다 새 트랜잭션을 연다.</p>
 */
@Configuration
@EnableRetry
public class RetryConfig {
}
