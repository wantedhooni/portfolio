package com.revy.batch.chapter_02.batch

import org.slf4j.LoggerFactory
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager

@Component
class HelloWorldBatch() {
    companion object {
        private val log = LoggerFactory.getLogger(javaClass)
    }

    @Bean("Step1")
    fun step(
        jobRepository: JobRepository, transactionManager: PlatformTransactionManager
    ): Step {
        log.info("step build")
        return StepBuilder("step1", jobRepository)
            .tasklet({ _, _ ->
                println("Hello, World!")
                log.info("LOG Hello, World!")
                RepeatStatus.FINISHED
            }, transactionManager).
        build()
    }

    @Bean("job")
    fun job(
        jobRepository: JobRepository,
        @Qualifier("Step1") step: Step,
    ): Job {
        log.info("job build")
        return JobBuilder("job", jobRepository)
            .start(step)
            .build()
    }
}
