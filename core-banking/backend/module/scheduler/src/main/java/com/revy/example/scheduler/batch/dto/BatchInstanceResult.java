package com.revy.example.scheduler.batch.dto;

import java.time.LocalDateTime;

public record BatchInstanceResult(
        Long id,
        String jobName,
        Long lastExecutionId,
        String lastStatus,
        String lastExitCode,
        LocalDateTime lastStartTime,
        LocalDateTime lastEndTime
) {}
