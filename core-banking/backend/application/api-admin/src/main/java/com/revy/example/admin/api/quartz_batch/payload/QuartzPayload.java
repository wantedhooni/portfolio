package com.revy.example.admin.api.quartz_batch.payload;

import com.revy.example.quartz.enums.JobType;
import com.revy.example.quartz.enums.QuartzJobExecutionStatus;
import com.revy.example.quartz.enums.ScheduleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * api-admin Quartz 제어/조회용 페이로드.
 * 모듈 {@code module/quartz-batch} 의 DTO 를 노출 API 형태로 매핑한다.
 */
public class QuartzPayload {

    // ── 조회 응답 ────────────────────────────────────────────────

    public record JobResponse(
            String jobName,
            String jobGroup,
            String triggerName,
            String triggerGroup,
            String triggerState,    // NORMAL / PAUSED / COMPLETE / ERROR / BLOCKED / NONE
            String triggerType,     // CRON / SimpleTriggerImpl 등
            String description,
            Instant previousFireTime,
            Instant nextFireTime
    ) {}

    public record RunningJobResponse(
            String jobName,
            String jobGroup,
            String triggerName,
            String triggerGroup,
            Instant fireTime,
            Instant scheduledFireTime,
            Long runningTimeMs
    ) {}

    public record HistoryResponse(
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
    ) {}

    // ── 등록/수정 요청 ──────────────────────────────────────────

    public record UpsertRequest(
            @NotBlank String jobName,
            @NotBlank String jobGroup,
            @NotNull  JobType jobType,
            @NotNull  ScheduleType scheduleType,
            String cronExpression,        // scheduleType=CRON 필수
            Long repeatIntervalMs,         // SIMPLE 필수 (ms)
            Integer repeatCount,           // SIMPLE 선택 (-1 = 무한)
            OffsetDateTime startAt,        // ONCE 필수, SIMPLE 선택
            String description,
            Map<String, String> jobData
    ) {}

    public record RescheduleRequest(
            @NotNull ScheduleType scheduleType,
            String cronExpression,
            Long repeatIntervalMs,
            Integer repeatCount,
            OffsetDateTime startAt
    ) {}
}
