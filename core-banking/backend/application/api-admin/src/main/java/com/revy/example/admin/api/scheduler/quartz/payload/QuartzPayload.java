package com.revy.example.admin.api.scheduler.quartz.payload;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class QuartzPayload {

    public record TriggerResponse(
            String name,
            String group,
            String jobName,
            String jobGroup,
            String description,
            String type,
            String cronExpression,
            Long repeatInterval,
            Integer repeatCount,
            Instant nextFireTime,
            Instant previousFireTime,
            Instant startTime,
            Instant endTime,
            String state
    ) {}

    public record JobResponse(
            String name,
            String group,
            String description,
            String jobClass,
            boolean durable,
            boolean concurrentExecutionDisallowed,
            boolean persistJobDataAfterExecution,
            boolean requestsRecovery,
            Map<String, Object> jobDataMap,
            List<TriggerResponse> triggers
    ) {}

    public record ExecutionResponse(
            Long id,
            String jobName,
            String jobGroup,
            String triggerName,
            String triggerGroup,
            Instant firedAt,
            Instant completedAt,
            Long runTimeMs,
            String result,
            String exceptionMessage,
            String instanceId
    ) {}
}
