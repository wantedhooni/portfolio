package com.revy.springbatchquartz.monitoring;

import java.time.LocalDateTime;

/**
 * 배치 스텝 실행 상세 응답 모델.
 */
public record StepExecutionResponse(
    Long stepExecutionId,
    String stepName,
    String status,
    int readCount,
    int writeCount,
    int rollbackCount,
    LocalDateTime startTime,
    LocalDateTime endTime
) {
}
