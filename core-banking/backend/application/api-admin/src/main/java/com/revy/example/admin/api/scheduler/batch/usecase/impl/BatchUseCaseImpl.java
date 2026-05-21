package com.revy.example.admin.api.scheduler.batch.usecase.impl;

import com.revy.example.admin.api.scheduler.batch.payload.BatchPayload;
import com.revy.example.admin.api.scheduler.batch.usecase.BatchUseCase;
import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.scheduler.batch.BatchCommand;
import com.revy.example.scheduler.batch.BatchReader;
import com.revy.example.scheduler.batch.dto.BatchExecutionResult;
import com.revy.example.scheduler.batch.dto.BatchInstanceResult;
import com.revy.example.scheduler.batch.dto.BatchJobResult;
import com.revy.example.scheduler.batch.dto.BatchStepExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BatchUseCaseImpl implements BatchUseCase {

    private final BatchReader  reader;
    private final BatchCommand command;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<BatchPayload.JobResponse> listJobs() {
        return reader.listJobs().stream().map(this::toJob).toList();
    }

    @Override
    public PageImpl<BatchPayload.InstanceResponse> searchInstances(String jobName, Pageable pageable) {
        Page<BatchInstanceResult> page = reader.searchInstances(jobName, pageable);
        return new PageImpl<>(page.getContent().stream().map(this::toInstance).toList(),
                pageable, page.getTotalElements());
    }

    @Override
    public PageImpl<BatchPayload.ExecutionResponse> searchExecutions(String jobName, String status, Pageable pageable) {
        Page<BatchExecutionResult> page = reader.searchExecutions(jobName, status, pageable);
        return new PageImpl<>(page.getContent().stream().map(this::toExecution).toList(),
                pageable, page.getTotalElements());
    }

    @Override
    public BatchPayload.ExecutionResponse getExecution(Long id) {
        return toExecution(reader.findExecution(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATCH_EXECUTION_NOT_FOUND)));
    }

    @Override
    public List<BatchPayload.ExecutionResponse> listRunning(String jobName) {
        return reader.listRunning(jobName).stream().map(this::toExecution).toList();
    }

    // ── Control ──────────────────────────────────────────────────

    @Override
    @Transactional
    public Long launch(BatchPayload.LaunchRequest req) {
        // CONTROL 노드에서는 BATCH_LAUNCH_NOT_SUPPORTED 발생.
        // 실제 워커 노드에서만 정상 동작.
        return command.launch(req.jobName(), req.jobParameters());
    }

    @Override
    @Transactional
    public Long requestLaunch(BatchPayload.LaunchRequest req, String requestedBy) {
        // batch_launch_request 큐에 INSERT 하면 워커가 폴링하여 실행
        return jdbcTemplate.queryForObject(
                "INSERT INTO batch_launch_request (job_name, job_parameters, requested_by) " +
                        "VALUES (?, ?, ?) RETURNING id",
                Long.class,
                req.jobName(), req.jobParameters(), requestedBy
        );
    }

    @Override
    @Transactional
    public void stop(Long executionId) { command.stop(executionId); }

    @Override
    @Transactional
    public void abandon(Long executionId) { command.abandon(executionId); }

    @Override
    @Transactional
    public Long restart(Long executionId) { return command.restart(executionId); }

    // ─────────────────────────────────────────────────────────────

    private BatchPayload.JobResponse toJob(BatchJobResult r) {
        return new BatchPayload.JobResponse(r.name(), r.instanceCount(),
                r.lastExecutionId(), r.lastStatus(), r.lastExitCode());
    }

    private BatchPayload.InstanceResponse toInstance(BatchInstanceResult r) {
        return new BatchPayload.InstanceResponse(r.id(), r.jobName(),
                r.lastExecutionId(), r.lastStatus(), r.lastExitCode(),
                r.lastStartTime(), r.lastEndTime());
    }

    private BatchPayload.ExecutionResponse toExecution(BatchExecutionResult r) {
        List<BatchPayload.StepResponse> steps = r.steps().stream()
                .map(this::toStep).toList();
        return new BatchPayload.ExecutionResponse(
                r.id(), r.jobInstanceId(), r.jobName(), r.status(),
                r.exitCode(), r.exitMessage(), r.createTime(),
                r.startTime(), r.endTime(), r.lastUpdated(),
                r.jobParameters(), steps
        );
    }

    private BatchPayload.StepResponse toStep(BatchStepExecutionResult s) {
        return new BatchPayload.StepResponse(
                s.id(), s.stepName(), s.status(), s.exitCode(), s.exitMessage(),
                s.readCount(), s.writeCount(), s.commitCount(), s.rollbackCount(),
                s.readSkipCount(), s.processSkipCount(), s.writeSkipCount(), s.filterCount(),
                s.startTime(), s.endTime()
        );
    }
}
