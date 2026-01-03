package com.revy.springbatchquartz.monitoring;

import java.time.LocalDateTime;

/**
 * 배치 잡 실행 이력 응답 모델.
 */
public record BatchExecutionResponse(
    Long executionId,
    String jobName,
    String status,
    LocalDateTime startTime,
    LocalDateTime endTime
) {
}
