package com.revy.springbatchquartz.quartz;

import com.revy.springbatchquartz.config.AppProperties;
import java.time.Instant;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 쿼츠 스케줄러에서 호출되어 스프링 배치 잡을 실행하는 잡 클래스.
 */
@Component
@DisallowConcurrentExecution
public class BatchQuartzJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(BatchQuartzJob.class);
    private final JobLauncher jobLauncher;
    private final org.springframework.batch.core.Job batchJob;
    private final AppProperties appProperties;

    /**
     * 쿼츠 잡을 생성한다.
     *
     * @param jobLauncher 배치 잡 런처
     * @param batchJob 배치 잡
     * @param appProperties 애플리케이션 설정
     */
    public BatchQuartzJob(JobLauncher jobLauncher,
                          @Qualifier("sampleJob") org.springframework.batch.core.Job batchJob,
                          AppProperties appProperties) {
        this.jobLauncher = jobLauncher;
        this.batchJob = batchJob;
        this.appProperties = appProperties;
    }

    /**
     * 쿼츠 트리거가 호출할 때 배치 잡을 실행한다.
     *
     * @param context 쿼츠 실행 컨텍스트
     * @throws JobExecutionException 실행 실패 시 예외
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            log.info("context:{}", context);
            JobParameters parameters = new JobParametersBuilder()
                .addString("jobName", appProperties.getBatch().getJobName())
                .addLong("scheduledTime", Instant.now().toEpochMilli())
                .toJobParameters();

            log.info("JobParameters: {}", parameters);
            log.info("batchJob:{}", batchJob);
            jobLauncher.run(batchJob, parameters);

        } catch (Exception ex) {
            log.error("배치 잡 실행 실패", ex);
            throw new JobExecutionException(ex);
        }
    }
}
