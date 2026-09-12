package com.revy.batch.chapter_04.job

import com.revy.batch.chapter_04.common.log
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.CallableTaskletAdapter
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import java.util.concurrent.Callable

@Configuration
@SpringBootApplication
class CallableTaskletConfiguration {

    @Bean
    fun callableJob(
        jobRepository: JobRepository,
        callableStep: Step
    ) : Job {
        log.info { "callable job started" }
        return JobBuilder("callableJob", jobRepository)
            .start(callableStep)
            .build()
    }

    @Bean
    fun callableStep(
        jobRepository: JobRepository,
        tasklet: CallableTaskletAdapter,
        transactionManager: PlatformTransactionManager
    ): Step {
        log.info { "callableStep started" }
        return StepBuilder("callableStep", jobRepository)
            .tasklet(tasklet, transactionManager)
            .build()
    }

    @Bean
    fun tasklet(): CallableTaskletAdapter {
        log.info { "tasklet started" }
        return CallableTaskletAdapter(callableObject())
    }


    @Bean
    fun callableObject(): Callable<RepeatStatus> {
        return Callable {
            println("This was executed in another thread")
            RepeatStatus.FINISHED;
        }
    }
}

fun main(args: Array<String>) {
    runApplication<CallableTaskletConfiguration>(*args)
}