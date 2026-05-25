package com.revy.example.quartz.dto;

import java.time.Instant;

public record QuartzRunningJobResult(
    String jobName,
    String jobGroup,
    String triggerName,
    String triggerGroup,
    Instant fireTime,
    Instant scheduledFireTime,
    Long runningTimeMs
) {
}
