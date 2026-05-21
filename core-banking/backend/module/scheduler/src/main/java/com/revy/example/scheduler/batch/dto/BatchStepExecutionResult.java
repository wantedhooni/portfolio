package com.revy.example.scheduler.batch.dto;

import java.time.LocalDateTime;

public record BatchStepExecutionResult(
        Long id,
        String stepName,
        String status,
        String exitCode,
        String exitMessage,
        long readCount,
        long writeCount,
        long commitCount,
        long rollbackCount,
        long readSkipCount,
        long processSkipCount,
        long writeSkipCount,
        long filterCount,
        LocalDateTime startTime,
        LocalDateTime endTime
) {}
