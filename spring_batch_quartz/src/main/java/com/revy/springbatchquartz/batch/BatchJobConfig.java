package com.revy.springbatchquartz.batch;

import com.revy.springbatchquartz.batch.listener.BatchJobListener;
import com.revy.springbatchquartz.batch.listener.BatchStepListener;
import com.revy.springbatchquartz.config.AppProperties;
import java.util.List;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 샘플 배치 잡과 스텝을 구성하는 설정 클래스.
 */
@Configuration
public class BatchJobConfig {

    /**
     * 배치 잡을 생성한다.
     *
     * @param jobRepository 잡 리포지토리
     * @param sampleStep 샘플 스텝
     * @param jobListener 잡 리스너
     * @param appProperties 애플리케이션 설정
     * @return 배치 잡
     */
    @Bean
    public Job sampleJob(JobRepository jobRepository,
                         Step sampleStep,
                         BatchJobListener jobListener,
                         AppProperties appProperties) {
        return new JobBuilder(appProperties.getBatch().getJobName(), jobRepository)
            .incrementer(new RunIdIncrementer())
            .listener(jobListener)
            .start(sampleStep)
            .build();
    }

    /**
     * 샘플 스텝을 생성한다.
     *
     * @param jobRepository 잡 리포지토리
     * @param transactionManager 트랜잭션 매니저
     * @param itemReader 아이템 리더
     * @param itemProcessor 아이템 프로세서
     * @param itemWriter 아이템 라이터
     * @param stepListener 스텝 리스너
     * @param appProperties 애플리케이션 설정
     * @return 배치 스텝
     */
    @Bean
    public Step sampleStep(JobRepository jobRepository,
                           PlatformTransactionManager transactionManager,
                           ItemReader<String> itemReader,
                           ItemProcessor<String, String> itemProcessor,
                           ItemWriter<String> itemWriter,
                           BatchStepListener stepListener,
                           AppProperties appProperties) {
        return new StepBuilder("sampleStep", jobRepository)
            .<String, String>chunk(3, transactionManager)
            .reader(itemReader)
            .processor(itemProcessor)
            .writer(itemWriter)
            .listener(stepListener)
            .faultTolerant()
            .retryPolicy(retryPolicy(appProperties))
            .retry(IllegalStateException.class)
            .backOffPolicy(backoffPolicy(appProperties))
            .build();
    }

    /**
     * 샘플 데이터를 제공하는 아이템 리더를 생성한다.
     *
     * @return 아이템 리더
     */
    @Bean
    public ItemReader<String> itemReader() {
        return new ListItemReader<>(List.of("alpha", "retry", "beta", "gamma"));
    }

    /**
     * 샘플 아이템 프로세서를 생성한다.
     *
     * @return 아이템 프로세서
     */
    @Bean
    public ItemProcessor<String, String> itemProcessor() {
        return new SampleItemProcessor();
    }

    /**
     * 샘플 아이템 라이터를 생성한다.
     *
     * @return 아이템 라이터
     */
    @Bean
    public ItemWriter<String> itemWriter() {
        return new SampleItemWriter();
    }

    /**
     * 지수 백오프 정책을 생성한다.
     *
     * @param appProperties 애플리케이션 설정
     * @return 백오프 정책
     */
    @Bean
    public ExponentialBackOffPolicy backoffPolicy(AppProperties appProperties) {
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(appProperties.getBatch().getRetry().getBackoff().getInitial());
        backOffPolicy.setMultiplier(appProperties.getBatch().getRetry().getBackoff().getMultiplier());
        backOffPolicy.setMaxInterval(appProperties.getBatch().getRetry().getBackoff().getMax());
        return backOffPolicy;
    }

    /**
     * 단순 재시도 정책을 생성한다.
     *
     * @param appProperties 애플리케이션 설정
     * @return 재시도 정책
     */
    @Bean
    public SimpleRetryPolicy retryPolicy(AppProperties appProperties) {
        return new SimpleRetryPolicy(appProperties.getBatch().getRetry().getLimit());
    }
}
