package com.revy.example.scheduler.batch.impl;

import com.revy.example.scheduler.batch.BatchReader;
import com.revy.example.scheduler.batch.dto.BatchExecutionResult;
import com.revy.example.scheduler.batch.dto.BatchInstanceResult;
import com.revy.example.scheduler.batch.dto.BatchJobResult;
import com.revy.example.scheduler.batch.dto.BatchStepExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameter;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Spring Batch 6 부터 {@code JobRepository} 가 {@code JobExplorer} 를 상속하여
 * 읽기·쓰기 통합 인터페이스가 되었다. 폐기 예정인 {@code JobExplorer} 대신 사용.
 */
@Component
@RequiredArgsConstructor
public class BatchReaderImpl implements BatchReader {

    private final JobRepository jobRepository;

    @Override
    public List<BatchJobResult> listJobs() {
        List<String> names = jobRepository.getJobNames();
        List<BatchJobResult> result = new ArrayList<>();
        for (String name : names) {
            int count = (int) Math.min(safeInstanceCount(name), Integer.MAX_VALUE);
            JobInstance latest = jobRepository.getLastJobInstance(name);
            JobExecution lastExec = (latest == null) ? null : jobRepository.getLastJobExecution(latest);
            result.add(new BatchJobResult(
                    name,
                    count,
                    lastExec == null ? null : lastExec.getId(),
                    lastExec == null ? null : lastExec.getStatus().name(),
                    lastExec == null ? null : lastExec.getExitStatus().getExitCode()
            ));
        }
        return result;
    }

    @Override
    public Page<BatchInstanceResult> searchInstances(String jobName, Pageable pageable) {
        long total = safeInstanceCount(jobName);
        if (total == 0L) return new PageImpl<>(List.of(), pageable, 0);

        List<JobInstance> instances = jobRepository.getJobInstances(
                jobName, (int) pageable.getOffset(), pageable.getPageSize());
        List<BatchInstanceResult> rows = new ArrayList<>();
        for (JobInstance inst : instances) {
            JobExecution last = jobRepository.getLastJobExecution(inst);
            rows.add(new BatchInstanceResult(
                    inst.getInstanceId(),
                    inst.getJobName(),
                    last == null ? null : last.getId(),
                    last == null ? null : last.getStatus().name(),
                    last == null ? null : last.getExitStatus().getExitCode(),
                    last == null ? null : last.getStartTime(),
                    last == null ? null : last.getEndTime()
            ));
        }
        return new PageImpl<>(rows, pageable, total);
    }

    @Override
    public Optional<BatchExecutionResult> findExecution(Long executionId) {
        JobExecution exec = jobRepository.getJobExecution(executionId);
        if (exec == null) return Optional.empty();
        return Optional.of(toResult(exec));
    }

    @Override
    public Page<BatchExecutionResult> searchExecutions(String jobName, String status, Pageable pageable) {
        // JobRepository 는 전 Job 페이징 검색을 직접 지원하지 않으므로 Job별로 순회.
        List<String> jobNames = (jobName == null || jobName.isBlank())
                ? jobRepository.getJobNames() : List.of(jobName);

        List<BatchExecutionResult> all = new ArrayList<>();
        for (String name : jobNames) {
            long count = safeInstanceCount(name);
            if (count == 0) continue;
            List<JobInstance> instances = jobRepository.getJobInstances(
                    name, 0, (int) Math.min(count, 200));
            for (JobInstance inst : instances) {
                for (JobExecution exec : jobRepository.getJobExecutions(inst)) {
                    if (status != null && !status.isBlank()
                            && !exec.getStatus().name().equalsIgnoreCase(status)) continue;
                    all.add(toResult(exec));
                }
            }
        }
        all.sort((a, b) -> Long.compare(b.id(), a.id()));
        int from = (int) Math.min(pageable.getOffset(), all.size());
        int to   = Math.min(from + pageable.getPageSize(), all.size());
        return new PageImpl<>(all.subList(from, to), pageable, all.size());
    }

    @Override
    public List<BatchExecutionResult> listRunning(String jobName) {
        List<String> jobNames = (jobName == null || jobName.isBlank())
                ? jobRepository.getJobNames() : List.of(jobName);
        List<BatchExecutionResult> result = new ArrayList<>();
        for (String name : jobNames) {
            for (JobExecution exec : jobRepository.findRunningJobExecutions(name)) {
                result.add(toResult(exec));
            }
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────

    private long safeInstanceCount(String jobName) {
        try {
            return jobRepository.getJobInstanceCount(jobName);
        } catch (NoSuchJobException e) {
            return 0L;
        }
    }

    private BatchExecutionResult toResult(JobExecution exec) {
        Map<String, String> params = new HashMap<>();
        JobParameters p = exec.getJobParameters();
        if (p != null) {
            for (JobParameter<?> jp : p) {
                params.put(jp.name(), jp.value() == null ? null : jp.value().toString());
            }
        }

        List<BatchStepExecutionResult> steps = new ArrayList<>();
        for (StepExecution s : exec.getStepExecutions()) {
            steps.add(new BatchStepExecutionResult(
                    s.getId(),
                    s.getStepName(),
                    s.getStatus().name(),
                    s.getExitStatus().getExitCode(),
                    s.getExitStatus().getExitDescription(),
                    s.getReadCount(),
                    s.getWriteCount(),
                    s.getCommitCount(),
                    s.getRollbackCount(),
                    s.getReadSkipCount(),
                    s.getProcessSkipCount(),
                    s.getWriteSkipCount(),
                    s.getFilterCount(),
                    s.getStartTime(),
                    s.getEndTime()
            ));
        }

        return new BatchExecutionResult(
                exec.getId(),
                exec.getJobInstanceId(),
                exec.getJobInstance() == null ? null : exec.getJobInstance().getJobName(),
                exec.getStatus().name(),
                exec.getExitStatus().getExitCode(),
                exec.getExitStatus().getExitDescription(),
                exec.getCreateTime(),
                exec.getStartTime(),
                exec.getEndTime(),
                exec.getLastUpdated(),
                params,
                steps
        );
    }
}
