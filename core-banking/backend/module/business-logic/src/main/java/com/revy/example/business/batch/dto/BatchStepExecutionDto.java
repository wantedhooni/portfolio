package com.revy.example.business.batch.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Spring Batch STEP_EXECUTION 한 건의 정보.
 */
@Getter
@Builder
public class BatchStepExecutionDto {
    private final Long stepExecutionId;
    private final String stepName;
    private final String status;
    private final String exitCode;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final Long durationMs;
    private final long readCount;
    private final long writeCount;
    private final long commitCount;
    private final long rollbackCount;
    private final long filterCount;
    private final long readSkipCount;
    private final long writeSkipCount;
    private final long processSkipCount;
    private final String exitMessage;
}
