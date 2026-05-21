package com.revy.example.scheduler.batch.impl;

import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.scheduler.batch.BatchCommand;
import com.revy.example.scheduler.config.SchedulerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.converter.JobParametersConverter;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.JobRestartException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Spring Batch 6 모범 사례:
 * <ul>
 *   <li>읽기/Execution 조회는 {@link JobRepository} (구 {@code JobExplorer}).</li>
 *   <li>제어는 {@link JobOperator} 의 {@link JobExecution} 오버로드를 사용 (long ID 오버로드 폐기됨).</li>
 *   <li>실행은 {@link JobRegistry} 로 {@link Job} 빈을 찾아 {@link JobOperator#start(Job, JobParameters)} 호출
 *       (구 {@code start(String, Properties)} 폐기됨).</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BatchCommandImpl implements BatchCommand {

    private final JobOperator jobOperator;
    private final JobRepository jobRepository;
    private final JobRegistry jobRegistry;
    private final JobParametersConverter jobParametersConverter;
    private final SchedulerProperties properties;

    @Override
    public Long launch(String jobName, String paramsString) {
        ensureWorkerMode();

        // Spring Batch 6 의 JobRegistry.getJob(String) 은 checked NoSuchJobException 을 더 이상
        // 선언하지 않는다. 미등록 시 null 또는 unchecked 변형이 던져질 수 있어 양쪽 모두 방어.
        Job job;
        try {
            job = jobRegistry.getJob(jobName);
        } catch (RuntimeException e) {
            throw new BusinessException(ErrorCode.BATCH_JOB_NOT_FOUND);
        }
        if (job == null) throw new BusinessException(ErrorCode.BATCH_JOB_NOT_FOUND);

        JobParameters params = jobParametersConverter.getJobParameters(parseProperties(paramsString));

        try {
            JobExecution execution = jobOperator.start(job, params);
            return execution.getId();
        } catch (JobInstanceAlreadyCompleteException
                 | JobExecutionAlreadyRunningException
                 | JobRestartException e) {
            throw new BusinessException(ErrorCode.SCHEDULER_OPERATION_FAILED, e.getMessage());
        } catch (InvalidJobParametersException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, e.getMessage());
        }
    }

    @Override
    public void stop(Long executionId) {
        JobExecution exec = jobRepository.getJobExecution(executionId);
        if (exec == null) throw new BusinessException(ErrorCode.BATCH_EXECUTION_NOT_FOUND);
        if (!exec.isRunning()) throw new BusinessException(ErrorCode.BATCH_EXECUTION_NOT_RUNNING);
        try {
            jobOperator.stop(exec);
        } catch (JobExecutionNotRunningException e) {
            throw new BusinessException(ErrorCode.BATCH_EXECUTION_NOT_RUNNING);
        }
    }

    @Override
    public void abandon(Long executionId) {
        JobExecution exec = jobRepository.getJobExecution(executionId);
        if (exec == null) throw new BusinessException(ErrorCode.BATCH_EXECUTION_NOT_FOUND);
        try {
            jobOperator.abandon(exec);
        } catch (JobExecutionAlreadyRunningException e) {
            throw new BusinessException(ErrorCode.SCHEDULER_OPERATION_FAILED, e.getMessage());
        }
    }

    @Override
    public Long restart(Long executionId) {
        ensureWorkerMode();
        JobExecution exec = jobRepository.getJobExecution(executionId);
        if (exec == null) throw new BusinessException(ErrorCode.BATCH_EXECUTION_NOT_FOUND);
        try {
            JobExecution restarted = jobOperator.restart(exec);
            return restarted.getId();
        } catch (JobRestartException e) {
            throw new BusinessException(ErrorCode.SCHEDULER_OPERATION_FAILED, e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────

    private void ensureWorkerMode() {
        if (properties.getMode() != SchedulerProperties.Mode.WORKER) {
            throw new BusinessException(ErrorCode.BATCH_LAUNCH_NOT_SUPPORTED);
        }
    }

    /** "key1=value1,key2=value2" 형식 → {@link Properties} (이후 JobParametersConverter 가 타입 변환) */
    private Properties parseProperties(String paramsString) {
        Properties props = new Properties();
        if (paramsString == null || paramsString.isBlank()) return props;
        for (String pair : paramsString.split(",")) {
            int eq = pair.indexOf('=');
            if (eq <= 0) continue;
            props.setProperty(pair.substring(0, eq).trim(), pair.substring(eq + 1).trim());
        }
        return props;
    }
}
