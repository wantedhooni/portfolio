package com.revy.batch.chapter_04.job


import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.job.DefaultJobParametersExtractor
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@EnableBatchProcessing
@SpringBootApplication
class JobJob {

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

    /**
     * Child Job
     */
    @Bean
    fun preProcessingJob(
        jobRepository: JobRepository,
        loadFileStep: Step,
        loadCustomerStep: Step,
        updateStartStep: Step,
    ): Job =
        JobBuilder("preProcessingJob", jobRepository)
            .start(loadFileStep)
            .next(loadCustomerStep)
            .next(updateStartStep)
            .build()

    /**
     * Parent Job
     */
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

    /**
     * Child Job을 실행하는 JobStep
     */
    @Bean
    fun initializeBatch(
        jobRepository: JobRepository,
        preProcessingJob: Job,
    ): Step =
        StepBuilder("initializeBatch", jobRepository)
            .job(preProcessingJob)
            .parametersExtractor(DefaultJobParametersExtractor())
            .build()

    @Bean
    fun loadFileStep(
        jobRepository: JobRepository,
        loadStockFile: Tasklet,
    ): Step =
        StepBuilder("loadFileStep", jobRepository)
            .tasklet(loadStockFile)
            .build()

    @Bean
    fun loadCustomerStep(
        jobRepository: JobRepository,
        loadCustomerFile: Tasklet,
    ): Step =
        StepBuilder("loadCustomerStep", jobRepository)
            .tasklet(loadCustomerFile)
            .build()

    @Bean
    fun updateStartStep(
        jobRepository: JobRepository,
        updateStart: Tasklet,
    ): Step =
        StepBuilder("updateStartStep", jobRepository)
            .tasklet(updateStart)
            .build()

    @Bean
    fun runBatch(
        jobRepository: JobRepository,
        runBatchTasklet: Tasklet,
    ): Step =
        StepBuilder("runBatch", jobRepository)
            .tasklet(runBatchTasklet)
            .build()
}

fun main(args: Array<String>) {
    runApplication<JobJob>(*args)
}