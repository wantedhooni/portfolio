package com.revy.example.quartz.dto;

import com.revy.example.quartz.enums.QuartzJobExecutionStatus;

import java.time.LocalDateTime;

public record QuartzJobExecutionHistoryResult(
    Long id,
    String schedulerName,
    String fireInstanceId,
    String jobName,
    String jobGroup,
    String triggerName,
    String triggerGroup,
    QuartzJobExecutionStatus status,
    LocalDateTime scheduledFireTime,
    LocalDateTime fireTime,
    LocalDateTime endTime,
    Long durationMs,
    String errorMessage
) {
}