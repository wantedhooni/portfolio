package com.example.querydslsqldemo.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyProductSalesStatJobLauncher {

    private final JobOperator jobOperator;

    @Qualifier("dailyProductSalesStatJob") private final Job dailyProductSalesStatJob;

    public DailyProductSalesStatJobResult run(LocalDate targetDate) {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString(DailyProductSalesStatBatchConfig.TARGET_DATE_PARAMETER, targetDate.toString())
                    .addLong("requestedAt", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution jobExecution = jobOperator.start(dailyProductSalesStatJob, jobParameters);

            List<DailyProductSalesStatStepResult> steps = jobExecution
                    .getStepExecutions()
                    .stream()
                    .map(DailyProductSalesStatStepResult::from)
                    .toList();

            return new DailyProductSalesStatJobResult(
                    jobExecution.getJobInstanceId(),
                    jobExecution.getId(),
                    jobExecution.getJobInstance().getJobName(),
                    targetDate,
                    jobExecution.getStatus(),
                    jobExecution.getExitStatus().getExitCode(),
                    steps
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to launch daily product sales stat batch job", e);
        }
    }

    public record DailyProductSalesStatJobResult(
            Long jobId,
            Long jobExecutionId,
            String jobName,
            LocalDate targetDate,
            BatchStatus status,
            String exitCode,
            List<DailyProductSalesStatStepResult> steps
    ) {
    }

    public record DailyProductSalesStatStepResult(
            Long stepExecutionId,
            String stepName,
            BatchStatus status,
            String exitCode,
            long readCount,
            long writeCount,
            long commitCount,
            long rollbackCount,
            long deletedRows,
            long insertedRows
    ) {
        static DailyProductSalesStatStepResult from(StepExecution stepExecution) {
            return new DailyProductSalesStatStepResult(
                    stepExecution.getId(),
                    stepExecution.getStepName(),
                    stepExecution.getStatus(),
                    stepExecution.getExitStatus() == null
                            ? ExitStatus.UNKNOWN.getExitCode()
                            : stepExecution.getExitStatus().getExitCode(),
                    stepExecution.getReadCount(),
                    stepExecution.getWriteCount(),
                    stepExecution.getCommitCount(),
                    stepExecution.getRollbackCount(),
                    stepExecution.getExecutionContext().getLong("deletedRows", 0L),
                    stepExecution.getExecutionContext().getLong("insertedRows", 0L)
            );
        }
    }
}
