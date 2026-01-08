package com.example.bulk_test_sample.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.quartz.JobBuilder;

/**
 * Quartz 스케줄러를 구성한다.
 */
@Slf4j
@Configuration
public class QuartzConfig {

    /**
     * Job 자동 주입을 위한 JobFactory를 생성한다.
     *
     * @param beanFactory 스프링 빈 팩토리
     * @return JobFactory
     */
    @Bean
    public JobFactory jobFactory(AutowireCapableBeanFactory beanFactory) {
        return new AutowiringSpringBeanJobFactory(beanFactory);
    }

    /**
     * SchedulerFactoryBean에 JobFactory를 등록한다.
     *
     * @param jobFactory JobFactory
     * @return SchedulerFactoryBean
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(JobFactory jobFactory) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setJobFactory(jobFactory);
        return factory;
    }

    /**
     * 배치 실행용 JobDetail을 정의한다.
     *
     * @return JobDetail
     */
    @Bean
    public JobDetail batchJobDetail() {
        return JobBuilder.newJob(BatchQuartzJob.class)
                .withIdentity("batchExportJob")
                .storeDurably()
                .build();
    }

    /**
     * 샘플 트리거를 정의한다.
     *
     * @param batchJobDetail JobDetail
     * @return Trigger
     */
    @Bean
    public Trigger sampleTrigger(JobDetail batchJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(batchJobDetail)
                .withIdentity("batchExportTrigger")
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInHours(24).repeatForever())
                .build();
    }

    @Bean
    public ApplicationRunner registerBatchJob(
            Scheduler scheduler,
            JobDetail batchJobDetail
    ) {
        return args -> {

            JobKey jobKey = batchJobDetail.getKey();
            log.info("jobKey.getName() :{}", jobKey.getName());
            if (!scheduler.checkExists(jobKey)) {
                scheduler.addJob(batchJobDetail, true);
                log.info("scheduler.addJob");
            }
        };
    }
}
