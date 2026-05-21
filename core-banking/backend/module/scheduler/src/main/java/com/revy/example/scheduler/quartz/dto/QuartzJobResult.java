package com.revy.example.scheduler.quartz.dto;

import java.util.List;
import java.util.Map;

public record QuartzJobResult(
        String name,
        String group,
        String description,
        String jobClass,
        boolean durable,
        boolean concurrentExecutionDisallowed,
        boolean persistJobDataAfterExecution,
        boolean requestsRecovery,
        Map<String, Object> jobDataMap,
        List<QuartzTriggerResult> triggers
) {}
