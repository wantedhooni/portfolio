package com.revy.example.quartz.domain;

import com.revy.example.quartz.enums.QuartzJobExecutionStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "qrtz_job_execution_history",
    indexes = {
        @Index(name = "idx_qrtz_job_history_job", columnList = "job_group, job_name"),
        @Index(name = "idx_qrtz_job_history_status", columnList = "status"),
        @Index(name = "idx_qrtz_job_history_fire_time", columnList = "fire_time")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_qrtz_job_history_fire_instance",
            columnNames = "fire_instance_id"
        )
    }
)
@Getter
public class QuartzJobExecutionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scheduler_name", length = 120)
    private String schedulerName;

    @Column(name = "fire_instance_id", nullable = false, length = 200)
    private String fireInstanceId;

    @Column(name = "job_name", nullable = false, length = 200)
    private String jobName;

    @Column(name = "job_group", nullable = false, length = 200)
    private String jobGroup;

    @Column(name = "trigger_name", length = 200)
    private String triggerName;

    @Column(name = "trigger_group", length = 200)
    private String triggerGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private QuartzJobExecutionStatus status;

    @Column(name = "scheduled_fire_time")
    private LocalDateTime scheduledFireTime;

    @Column(name = "fire_time", nullable = false)
    private LocalDateTime fireTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "refire_count")
    private Integer refireCount;

    @Column(name = "recovering")
    private Boolean recovering;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "error_stack_trace", columnDefinition = "TEXT")
    private String errorStackTrace;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected QuartzJobExecutionHistory() {
    }

    public static QuartzJobExecutionHistory running(
        String schedulerName,
        String fireInstanceId,
        String jobName,
        String jobGroup,
        String triggerName,
        String triggerGroup,
        LocalDateTime scheduledFireTime,
        LocalDateTime fireTime,
        int refireCount,
        boolean recovering
    ) {
        QuartzJobExecutionHistory history = new QuartzJobExecutionHistory();
        history.schedulerName = schedulerName;
        history.fireInstanceId = fireInstanceId;
        history.jobName = jobName;
        history.jobGroup = jobGroup;
        history.triggerName = triggerName;
        history.triggerGroup = triggerGroup;
        history.scheduledFireTime = scheduledFireTime;
        history.fireTime = fireTime;
        history.status = QuartzJobExecutionStatus.RUNNING;
        history.refireCount = refireCount;
        history.recovering = recovering;
        return history;
    }

    public void success(LocalDateTime endTime, long durationMs) {
        this.status = QuartzJobExecutionStatus.SUCCESS;
        this.endTime = endTime;
        this.durationMs = durationMs;
    }

    public void failed(
        LocalDateTime endTime,
        long durationMs,
        String errorMessage,
        String errorStackTrace
    ) {
        this.status = QuartzJobExecutionStatus.FAILED;
        this.endTime = endTime;
        this.durationMs = durationMs;
        this.errorMessage = errorMessage;
        this.errorStackTrace = errorStackTrace;
    }

    public void vetoed(LocalDateTime endTime) {
        this.status = QuartzJobExecutionStatus.VETOED;
        this.endTime = endTime;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}