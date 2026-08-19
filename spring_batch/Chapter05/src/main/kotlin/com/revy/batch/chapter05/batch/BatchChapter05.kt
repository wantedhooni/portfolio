package com.revy.batch.chapter05.batch

import com.revy.batch.chapter05.ExploringTasklet
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


private val log = KotlinLogging.logger {}

@Configuration
@EnableBatchProcessing
class BatchChapter05(
    val jobRepository: JobRepository
) {
    @Bean
    fun explorerJob(): Job {
        log.info { "Bean Explorer job" }
        return JobBuilder("explorerJob", jobRepository)
            .start(explorerStep())
            .build()
    }

    @Bean
    fun explorerStep(): Step {
        log.info { "Bean Explorer step" }
        return StepBuilder("explorerStep", jobRepository)
            .tasklet(explorerTasklet())
            .build()
    }

    @Bean
    fun explorerTasklet(): Tasklet {
        log.info { "Bean Explorer tasklet" }
        return ExploringTasklet(jobRepository)
    }
}