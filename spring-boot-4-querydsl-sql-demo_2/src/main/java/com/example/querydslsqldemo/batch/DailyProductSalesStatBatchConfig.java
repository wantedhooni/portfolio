package com.example.querydslsqldemo.batch;

import com.example.querydslsqldemo.repository.DailyProductSalesStatJpaQueryRepository;
import java.time.LocalDate;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class DailyProductSalesStatBatchConfig {

    public static final String JOB_NAME = "dailyProductSalesStatJob";
    public static final String STEP_NAME = "recreateDailyProductSalesStatStep";
    public static final String TARGET_DATE_PARAMETER = "targetDate";

    private final DailyProductSalesStatJpaQueryRepository repository;

    @Bean
    public Job dailyProductSalesStatJob(
            JobRepository jobRepository,
            Step recreateDailyProductSalesStatStep
                                       ) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(recreateDailyProductSalesStatStep)
                .build();
    }

    @Bean
    public Step recreateDailyProductSalesStatStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager
    ) {
        return new StepBuilder(STEP_NAME, jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    Map<String, Object> jobParameters = chunkContext
                            .getStepContext()
                            .getJobParameters();

                    Object targetDateValue = jobParameters.get(TARGET_DATE_PARAMETER);
                    if (targetDateValue == null) {
                        throw new IllegalArgumentException("Job parameter 'targetDate' is required. Example: 2026-05-01");
                    }

                    LocalDate targetDate = LocalDate.parse(targetDateValue.toString());

                    long deletedRows = repository.deleteDailyStat(targetDate);
                    long insertedRows = repository.createDailyStatByQuerydslJpa(targetDate);

                    contribution.getStepExecution()
                            .getExecutionContext()
                            .putLong("deletedRows", deletedRows);
                    contribution.getStepExecution()
                            .getExecutionContext()
                            .putLong("insertedRows", insertedRows);

                    contribution.incrementWriteCount(insertedRows);

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
