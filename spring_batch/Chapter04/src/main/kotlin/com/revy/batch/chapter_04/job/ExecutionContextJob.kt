package com.revy.batch.chapter_04.job

import com.revy.batch.chapter_04.batch.HelloWorldTasklet
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@SpringBootApplication
class ExecutionContextJob {

    @Bean
    fun helloWorldBatchJob(
        jobRepository: JobRepository,
        helloWorldStep: Step,
    ): Job {
        return JobBuilder("helloWorldBatchJob", jobRepository)
            .start(helloWorldStep)
            .build()
    }


    @Bean
    fun helloWorldStep(
        jobRepository: JobRepository,
        tasklet: HelloWorldTasklet,
    ): Step {
        return StepBuilder("helloWorldStep", jobRepository)
            .tasklet(tasklet)
            .build()
    }


    @Bean
    @StepScope
    fun tasklet(): HelloWorldTasklet {
        return HelloWorldTasklet()
    }
}

fun main(args: Array<String>) {
    runApplication<ExecutionContextJob>(*args)
}