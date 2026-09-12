package com.revy.batch.chapter06.batch.job

import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.job.parameters.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager

@Component
class SampleBatchJob(
    val jobRepository: JobRepository
) {

    @Bean
    fun job(
        jobRepository: JobRepository,
        step1: Step,
    ): Job = JobBuilder("job", jobRepository).incrementer(RunIdIncrementer()).start(step1).build()

    @Bean
    fun step1(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
    ): Step = StepBuilder("step1", jobRepository).tasklet(
            { _, _ ->
                println("step1 ran!")
                RepeatStatus.FINISHED
            },
            transactionManager,
        ).build()

}