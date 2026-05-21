package com.revy.example.scheduler.quartz.dto;

import java.time.Instant;

public record QuartzTriggerResult(
        String name,
        String group,
        String jobName,
        String jobGroup,
        String description,
        String type,         // CRON / SIMPLE
        String cronExpression,
        Long   repeatInterval,
        Integer repeatCount,
        Instant nextFireTime,
        Instant previousFireTime,
        Instant startTime,
        Instant endTime,
        String state         // NORMAL / PAUSED / COMPLETE / ERROR / BLOCKED / NONE
) {}
