package com.revy.batch.chapter_04.job

import com.revy.batch.chapter_04.batch.DailyJobTimestamper
import com.revy.batch.chapter_04.batch.JobLoggerListener
import com.revy.batch.chapter_04.batch.ParameterValidator
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.job.parameters.CompositeJobParametersValidator
import org.springframework.batch.core.job.parameters.DefaultJobParametersValidator
import org.springframework.batch.core.listener.JobListenerFactoryBean
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
@SpringBootApplication
class HelloWorldJob {
    @Bean
    fun validator(): CompositeJobParametersValidator {
        val defaultValidator = DefaultJobParametersValidator(
            arrayOf("fileName"),
            arrayOf("name", "currentDate"),
        ).apply {
            afterPropertiesSet()
        }

        return CompositeJobParametersValidator().apply {
            setValidators(
                listOf(
                    ParameterValidator(),
                    defaultValidator,
                )
            )
        }
    }

    @Bean
    fun job(
        jobRepository: JobRepository,
        step1: Step,
        validator: CompositeJobParametersValidator,
    ): Job = JobBuilder("basicJob", jobRepository).start(step1).validator(validator).incrementer(DailyJobTimestamper())
        .listener(
            JobListenerFactoryBean.getListener(
                JobLoggerListener()
            )
        ).build()

    @Bean
    fun step1(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        helloWorldTasklet: Tasklet,
    ): Step = StepBuilder("step1", jobRepository).tasklet(
            helloWorldTasklet,
            transactionManager,
        ).build()

    @Bean
    @StepScope
    fun helloWorldTasklet(
        @Value("#{jobParameters['name']}")
        name: String?,

        @Value("#{jobParameters['fileName']}")
        fileName: String?,
    ): Tasklet = Tasklet { _, _ ->

        println("Hello, $name!")
        println("fileName = $fileName")

        RepeatStatus.FINISHED
    }
}

fun main(args: Array<String>) {
    runApplication<HelloWorldJob>(*args)
}