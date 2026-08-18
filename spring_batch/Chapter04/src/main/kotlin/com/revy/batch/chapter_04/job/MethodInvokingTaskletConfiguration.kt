package com.revy.batch.chapter_04.job

import com.revy.batch.chapter_04.service.CustomService
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.MethodInvokingTaskletAdapter
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@SpringBootApplication
class MethodInvokingTaskletConfiguration {

    @Bean
    fun methodInvokingJob(
        jobRepository: JobRepository,
        methodInvokingStep: Step,
    ): Job =
        JobBuilder("methodInvokingJob", jobRepository)
            .start(methodInvokingStep)
            .build()

    @Bean
    fun methodInvokingStep(
        jobRepository: JobRepository,
        methodInvokingTasklet: MethodInvokingTaskletAdapter,
    ): Step =
        StepBuilder("methodInvokingStep", jobRepository)
            .tasklet(methodInvokingTasklet)
            .build()

    @Bean
    @StepScope
    fun methodInvokingTasklet(
        @Value("#{jobParameters['message']}")
        message: String?,
        service: CustomService,
    ): MethodInvokingTaskletAdapter =
        MethodInvokingTaskletAdapter().apply {
            setTargetObject(service)
            setTargetMethod("serviceMethod")
            setArguments(arrayOf(message))
        }

    @Bean
    fun service(): CustomService =
        CustomService()
}

fun main(args: Array<String>) {
    runApplication<MethodInvokingTaskletConfiguration>(*args)
}