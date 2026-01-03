package com.revy.springbatchquartz.monitoring;

/**
 * 쿼츠 트리거 상태 응답 모델.
 */
public record QuartzTriggerResponse(
    String triggerName,
    String triggerGroup,
    String status,
    String nextFireTime
) {
}
