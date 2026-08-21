package com.revy.batch.chapter06.quartz.config

import com.revy.batch.chapter06.quartz.BatchScheduledJob
import org.quartz.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class QuartzConfiguration {
    @Bean
    fun quartzJobDetail(): JobDetail {
        return JobBuilder.newJob(BatchScheduledJob::class.java).storeDurably().build()
    }

    @Bean
    fun quartzJobTrigger(): Trigger {
        val simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(5).withRepeatCount(4);
        return TriggerBuilder.newTrigger().forJob(quartzJobDetail()).withSchedule(simpleScheduleBuilder).build();

    }
}