package com.revy.example.quartz.enums;

import com.revy.example.common.enums.ExposedEnum;

/**
 * Quartz Job 유형 식별자.
 *
 * <p>매핑 전략: 각 Job 구현체가 {@code TypedJob#getType()}으로 자기 유형을 선언하며,
 * 실행 노드에서 모든 {@code TypedJob} 빈을 수집해 {@code (JobType → Class)} 레지스트리를
 * 빌드합니다. {@link JobType}은 식별자 역할만 합니다.
 *
 * <p>{@link ExposedEnum} 구현으로 프론트엔드 메타 API에 자동 노출됩니다.
 */
public enum JobType implements ExposedEnum {

    SETTLEMENT("정산"), EXCHANGE_RATE_REFRESH("환율 자동 갱신");

    private final String description;

    JobType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

