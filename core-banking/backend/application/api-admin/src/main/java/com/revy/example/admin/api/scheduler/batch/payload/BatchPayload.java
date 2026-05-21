package com.revy.example.admin.api.scheduler.batch.payload;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class BatchPayload {

    public record JobResponse(
            String name,
            int instanceCount,
            Long lastExecutionId,
            String lastStatus,
            String lastExitCode
    ) {}

    public record InstanceResponse(
            Long id,
            String jobName,
            Long lastExecutionId,
            String lastStatus,
            String lastExitCode,
            LocalDateTime lastStartTime,
            LocalDateTime lastEndTime
    ) {}

    public record StepResponse(
            Long id,
            String stepName,
            String status,
            String exitCode,
            String exitMessage,
            long readCount,
            long writeCount,
            long commitCount,
            long rollbackCount,
            long readSkipCount,
            long processSkipCount,
            long writeSkipCount,
            long filterCount,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {}

    public record ExecutionResponse(
            Long id,
            Long jobInstanceId,
            String jobName,
            String status,
            String exitCode,
            String exitMessage,
            LocalDateTime createTime,
            LocalDateTime startTime,
            LocalDateTime endTime,
            LocalDateTime lastUpdated,
            Map<String, String> jobParameters,
            List<StepResponse> steps
    ) {}

    public record LaunchRequest(
            @NotBlank String jobName,
            String jobParameters    // "key1=value1,key2=value2" 형태 (Spring Batch 표준)
    ) {}
}
