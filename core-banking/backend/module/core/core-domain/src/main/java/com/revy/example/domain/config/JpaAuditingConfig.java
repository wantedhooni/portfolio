package com.revy.example.domain.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Spring Data JPA Auditing 활성화 설정.
 *
 * <p>{@link com.revy.example.domain.common.BaseEntity}의
 * {@code @CreatedDate} / {@code @LastModifiedDate} 가 동작하려면
 * {@code @EnableJpaAuditing} 이 Spring 컨텍스트에 반드시 선언되어야 합니다.</p>
 *
 * <p>이 클래스를 {@code core-domain} 모듈에 둠으로써
 * BaseEntity 를 상속하는 모든 엔티티가 별도 설정 없이 자동으로 Auditing 혜택을 받습니다.</p>
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
