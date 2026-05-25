package com.revy.example.quartz.dto;

import com.revy.example.quartz.enums.JobType;
import com.revy.example.quartz.enums.ScheduleType;

import java.time.OffsetDateTime;
import java.util.Map;

public record QuartzJobUpsertCommand(

    String jobName,

    String jobGroup,

    JobType jobType,

    ScheduleType scheduleType,

    String cronExpression,

    Long repeatIntervalMs,

    Integer repeatCount,

    OffsetDateTime startAt,

    String description,

    Map<String, String> jobData

) {
}