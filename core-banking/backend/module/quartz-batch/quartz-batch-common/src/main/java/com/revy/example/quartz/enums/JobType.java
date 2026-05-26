package com.revy.example.quartz.enums;

/**
 * Quartz Job 유형 식별자.
 *
 * <p>매핑 전략: 각 Job 구현체가 {@code TypedJob#getType()}으로 자기 유형을 선언하며,
 * 실행 노드에서 모든 {@code TypedJob} 빈을 수집해 {@code (JobType → Class)} 레지스트리를
 * 빌드합니다. {@link JobType}은 식별자 역할만 합니다.
 */
public enum JobType {
    SETTLEMENT,
    EXCHANGE_RATE_REFRESH,
    REPORT,
    NOTIFICATION,
    API_CALL
}
