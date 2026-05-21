package com.revy.example.scheduler.batch.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record BatchExecutionResult(
        Long id,
        Long jobInstanceId,
        String jobName,
        String status,
        String exitCode,
        String exitMessage,
        LocalDateTime createTime,
        LocalDateTime startTime,
        LocalDateTime endTime,
        LocalDateTime lastUpdated,
        Map<String, String> jobParameters,
        List<BatchStepExecutionResult> steps
) {}
