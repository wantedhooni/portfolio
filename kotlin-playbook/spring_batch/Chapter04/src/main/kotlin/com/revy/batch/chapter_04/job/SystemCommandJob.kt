package com.revy.batch.chapter_04.job

import org.springframework.batch.core.job.*
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.SystemCommandTasklet
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
@SpringBootApplication
class SystemCommandJob {
    @Bean
    fun job(
        jobRepository: JobRepository,
        systemCommandStep: Step,
    ): Job =
        JobBuilder("systemCommandJob", jobRepository)
            .start(systemCommandStep)
            .build()

    @Bean
    fun systemCommandStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        systemCommandTasklet: SystemCommandTasklet,
    ): Step =
        StepBuilder("systemCommandStep", jobRepository)
            .tasklet(systemCommandTasklet, transactionManager)
            .build()

    @Bean
    fun systemCommandTasklet(): SystemCommandTasklet =
        SystemCommandTasklet().apply {
            setCommand("rm -rf /tmp.txt")
            setTimeout(5_000)
            setInterruptOnCancel(true)
        }
}

fun main(args: Array<String>) {
    runApplication<SystemCommandJob>(*args)
}