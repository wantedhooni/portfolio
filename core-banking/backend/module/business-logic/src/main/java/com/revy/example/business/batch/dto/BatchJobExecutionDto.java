package com.revy.example.business.batch.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Spring Batch JOB_EXECUTION 한 건의 요약 정보.
 */
@Getter
@Builder
public class BatchJobExecutionDto {
    private final Long jobExecutionId;
    private final Long jobInstanceId;
    private final String jobName;
    private final String status;
    private final String exitCode;
    private final LocalDateTime createTime;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    /** 실행 소요 시간(ms). start/end 가 없으면 null. */
    private final Long durationMs;
}
