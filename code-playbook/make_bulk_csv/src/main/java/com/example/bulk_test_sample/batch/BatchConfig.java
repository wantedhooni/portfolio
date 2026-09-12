package com.example.bulk_test_sample.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch 구성을 정의한다.
 */
@Configuration
@EnableBatchProcessing
public class BatchConfig {

    /**
     * Tasklet 기반 Job을 구성한다.
     *
     * @param jobRepository JobRepository
     * @param taskletStep Tasklet 스텝
     * @return Job
     */
    @Bean
    public Job csvExportTaskletJob(JobRepository jobRepository, Step taskletStep) {
        return new JobBuilder("csvExportTaskletJob", jobRepository)
                .start(taskletStep)
                .build();
    }

    /**
     * Chunk 기반 Job을 구성한다.
     *
     * @param jobRepository JobRepository
     * @param chunkStep Chunk 스텝
     * @return Job
     */
    @Bean
    public Job csvExportChunkJob(JobRepository jobRepository, Step chunkStep) {
        return new JobBuilder("csvExportChunkJob", jobRepository)
                .start(chunkStep)
                .build();
    }

    /**
     * Tasklet 스텝을 구성한다.
     *
     * @param jobRepository JobRepository
     * @param transactionManager 트랜잭션 매니저
     * @param tasklet Tasklet
     * @return Step
     */
    @Bean
    public Step taskletStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            CsvExportTasklet tasklet
    ) {
        return new StepBuilder("taskletStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    /**
     * Chunk 스텝을 구성한다.
     *
     * @param jobRepository JobRepository
     * @param transactionManager 트랜잭션 매니저
     * @param reader ItemReader
     * @param writer ItemWriter
     * @return Step
     */
    @Bean
    public Step chunkStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            CsvRowItemReader reader,
            CsvRowItemWriter writer
    ) {
        return new StepBuilder("chunkStep", jobRepository)
                .<String[], String[]>chunk(1000, transactionManager)
                .reader(reader)
                .writer(writer)
                .listener(writer)
                .build();
    }

    /**
     * 비동기 JobLauncher를 구성한다.
     *
     * @param jobRepository JobRepository
     * @return JobLauncher
     */
    @Bean
    public JobLauncher jobLauncher(JobRepository jobRepository) throws Exception {
        TaskExecutorJobLauncher launcher = new TaskExecutorJobLauncher();
        launcher.setJobRepository(jobRepository);
        launcher.setTaskExecutor(new SimpleAsyncTaskExecutor("batch-job-"));
        launcher.afterPropertiesSet();
        return launcher;
    }
}
