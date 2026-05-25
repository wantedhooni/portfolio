package com.revy.example.quartz.dto;

import java.time.Instant;

public record QuartzJobResult(
    String jobName,
    String jobGroup,
    String triggerName,
    String triggerGroup,
    String triggerState,
    String triggerType,
    String description,
    Instant previousFireTime,
    Instant nextFireTime
) {
}