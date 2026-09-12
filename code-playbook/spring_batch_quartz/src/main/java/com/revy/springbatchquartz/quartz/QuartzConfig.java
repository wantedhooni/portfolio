package com.revy.springbatchquartz.quartz;

import com.revy.springbatchquartz.config.AppProperties;
import org.quartz.JobDetail;
import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.JobBuilder;
import org.quartz.CronScheduleBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 쿼츠 잡과 트리거를 등록하는 설정 클래스.
 */
@Configuration
public class QuartzConfig {

    /**
     * 배치 실행용 쿼츠 잡 디테일을 생성한다.
     *
     * @return 잡 디테일
     */
    @Bean
    public JobDetail batchJobDetail() {
        return JobBuilder.newJob(BatchQuartzJob.class)
            .withIdentity("batchQuartzJob")
            .storeDurably()
            .build();
    }

    /**
     * 크론 기반 트리거를 생성한다.
     *
     * @param batchJobDetail 잡 디테일
     * @param appProperties 애플리케이션 설정
     * @return 트리거
     */
    @Bean
    public Trigger batchJobTrigger(JobDetail batchJobDetail, AppProperties appProperties) {
        return TriggerBuilder.newTrigger()
            .forJob(batchJobDetail)
            .withIdentity("batchQuartzTrigger")
            .withSchedule(SimpleScheduleBuilder.simpleSchedule())
            .build();
    }
}
