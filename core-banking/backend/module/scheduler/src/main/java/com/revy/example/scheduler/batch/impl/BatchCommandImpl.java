package com.revy.example.scheduler.batch.impl;

import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.scheduler.batch.BatchCommand;
import com.revy.example.scheduler.config.SchedulerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobInstanceAlreadyExistsException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.JobRestartException;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.repository.explore.JobExplorer;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchCommandImpl implements BatchCommand {

    private final JobOperator jobOperator;
    private final JobExplorer jobExplorer;
    private final SchedulerProperties properties;

    @Override
    public Long launch(String jobName, String paramsJson) {
        ensureWorkerMode();
        try {
            return jobOperator.start(jobName, parseParameters(paramsJson));
        } catch (NoSuchJobException e) {
            throw new BusinessException(ErrorCode.BATCH_JOB_NOT_FOUND);
        } catch (JobInstanceAlreadyExistsException e) {
            throw new BusinessException(ErrorCode.SCHEDULER_OPERATION_FAILED, e.getMessage());
        } catch (InvalidJobParametersException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, e.getMessage());
        }
    }

    @Override
    public void stop(Long executionId) {
        JobExecution exec = jobExplorer.getJobExecution(executionId);
        if (exec == null) throw new BusinessException(ErrorCode.BATCH_EXECUTION_NOT_FOUND);
        if (!exec.isRunning()) throw new BusinessException(ErrorCode.BATCH_EXECUTION_NOT_RUNNING);
        try {
            jobOperator.stop(executionId);
        } catch (NoSuchJobExecutionException e) {
            throw new BusinessException(ErrorCode.BATCH_EXECUTION_NOT_FOUND);
        } catch (JobExecutionNotRunningException e) {
            throw new BusinessException(ErrorCode.BATCH_EXECUTION_NOT_RUNNING);
        }
    }

    @Override
    public void abandon(Long executionId) {
        JobExecution exec = jobExplorer.getJobExecution(executionId);
        if (exec == null) throw new BusinessException(ErrorCode.BATCH_EXECUTION_NOT_FOUND);
        try {
            jobOperator.abandon(executionId);
        } catch (NoSuchJobExecutionException e) {
            throw new BusinessException(ErrorCode.BATCH_EXECUTION_NOT_FOUND);
        } catch (JobExecutionAlreadyRunningException e) {
            throw new BusinessException(ErrorCode.SCHEDULER_OPERATION_FAILED, e.getMessage());
        }
    }

    @Override
    public Long restart(Long executionId) {
        ensureWorkerMode();
        try {
            return jobOperator.restart(executionId);
        } catch (NoSuchJobExecutionException e) {
            throw new BusinessException(ErrorCode.BATCH_EXECUTION_NOT_FOUND);
        } catch (NoSuchJobException e) {
            throw new BusinessException(ErrorCode.BATCH_JOB_NOT_FOUND);
        } catch (JobRestartException | JobInstanceAlreadyCompleteException
                 | InvalidJobParametersException e) {
            throw new BusinessException(ErrorCode.SCHEDULER_OPERATION_FAILED, e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────

    private void ensureWorkerMode() {
        if (properties.getMode() != SchedulerProperties.Mode.WORKER) {
            throw new BusinessException(ErrorCode.BATCH_LAUNCH_NOT_SUPPORTED);
        }
    }

    /** "key1=value1,key2=value2" 형식 → Properties */
    private Properties parseParameters(String paramsString) {
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
