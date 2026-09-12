package com.example.bulk_test_sample.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

/**
 * Quartz에서 Spring Batch Job을 실행한다.
 */
@Component
public class BatchQuartzJob implements Job {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    /**
     * 배치 실행에 필요한 구성 요소를 주입받는다.
     *
     * @param jobLauncher JobLauncher
     * @param jobRegistry JobRegistry
     */
    public BatchQuartzJob(JobLauncher jobLauncher, JobRegistry jobRegistry) {
        this.jobLauncher = jobLauncher;
        this.jobRegistry = jobRegistry;
    }

    /**
     * Quartz 트리거에 의해 배치 Job을 실행한다.
     *
     * @param context 실행 컨텍스트
     * @throws JobExecutionException 실행 실패
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String jobName = context.getMergedJobDataMap().getString("jobName");
        String jobUuid = context.getMergedJobDataMap().getString("jobUuid");
        JobParameters parameters = new JobParametersBuilder()
                .addString("jobUuid", jobUuid)
                .addLong("scheduledAt", System.currentTimeMillis())
                .toJobParameters();
        try {
            jobLauncher.run(jobRegistry.getJob(jobName), parameters);
        } catch (Exception ex) {
            throw new JobExecutionException("Quartz 배치 실행 실패", ex);
        }
    }
}
