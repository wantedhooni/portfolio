package com.revy.batch.chapter_04.job

import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.FlowBuilder
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.job.flow.Flow
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
@SpringBootApplication
class FlowJob {
    @Bean
    fun loadStockFile(): Tasklet =
        Tasklet { _, _ ->
            println("The stock file has been loaded")
            RepeatStatus.FINISHED
        }

    @Bean
    fun loadCustomerFile(): Tasklet =
        Tasklet { _, _ ->
            println("The customer file has been loaded")
            RepeatStatus.FINISHED
        }

    @Bean
    fun updateStart(): Tasklet =
        Tasklet { _, _ ->
            println("The start has been updated")
            RepeatStatus.FINISHED
        }

    @Bean
    fun runBatchTasklet(): Tasklet =
        Tasklet { _, _ ->
            println("The batch has been run")
            RepeatStatus.FINISHED
        }

    @Bean
    fun preProcessingFlow(
        loadFileStep: Step,
        loadCustomerStep: Step,
        updateStartStep: Step,
    ): Flow =
        FlowBuilder<Flow>("preProcessingFlow")
            .start(loadFileStep)
            .next(loadCustomerStep)
            .next(updateStartStep)
            .build()

    @Bean
    fun conditionalStepLogicJob(
        jobRepository: JobRepository,
        initializeBatch: Step,
        runBatch: Step,
    ): Job =
        JobBuilder("conditionalStepLogicJob", jobRepository)
            .start(initializeBatch)
            .next(runBatch)
            .build()

    @Bean
    fun initializeBatch(
        jobRepository: JobRepository,
        preProcessingFlow: Flow,
    ): Step =
        StepBuilder("initializeBatch", jobRepository)
            .flow(preProcessingFlow)
            .build()

    @Bean
    fun loadFileStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        loadStockFile: Tasklet,
    ): Step =
        StepBuilder("loadFileStep", jobRepository)
            .tasklet(loadStockFile, transactionManager)
            .build()

    @Bean
    fun loadCustomerStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        loadCustomerFile: Tasklet,
    ): Step =
        StepBuilder("loadCustomerStep", jobRepository)
            .tasklet(loadCustomerFile, transactionManager)
            .build()

    @Bean
    fun updateStartStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        updateStart: Tasklet,
    ): Step =
        StepBuilder("updateStartStep", jobRepository)
            .tasklet(updateStart, transactionManager)
            .build()

    @Bean
    fun runBatch(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        runBatchTasklet: Tasklet,
    ): Step =
        StepBuilder("runBatch", jobRepository)
            .tasklet(runBatchTasklet, transactionManager)
            .build()
}

fun main(args: Array<String>) {
    runApplication<FlowJob>(*args)
}