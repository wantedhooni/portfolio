package com.revy.example.scheduler.quartz.dto;

import java.time.Instant;

public record QuartzExecutionResult(
        Long id,
        String jobName,
        String jobGroup,
        String triggerName,
        String triggerGroup,
        Instant firedAt,
        Instant completedAt,
        Long   runTimeMs,
        String result,        // SUCCESS / FAILED / VETOED
        String exceptionMessage,
        String instanceId
) {}
