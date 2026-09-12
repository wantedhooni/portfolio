package com.revy.batch.chapter_04.job

import com.revy.batch.chapter_04.batch.RandomDecider
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.job.flow.JobExecutionDecider
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
class ConditionalJob {

    @Bean
    fun conditionalJob(
        jobRepository: JobRepository,
        firstStep: Step,
        successStep: Step
    ): Job {
        return JobBuilder("conditionalJob", jobRepository)
            .start(firstStep)
            .on("FAILED").stopAndRestart(successStep) // FAILED 일 때 중지 후 재시작 지점 지정
            .from(firstStep)
            .on("*").to(successStep) // 그 외 모든 상태(*)일 때 successStep으로 이동
            .end()
            .build()
    }


    @Bean
    fun firstStep(jobRepository: JobRepository, transactionManager: PlatformTransactionManager): Step {
        return StepBuilder("firstStep", jobRepository)
            .tasklet(passTasklet(), transactionManager) // Batch 6 필수: transactionManager 명시
            .build()
    }

    @Bean
    fun successStep(jobRepository: JobRepository, transactionManager: PlatformTransactionManager): Step {
        return StepBuilder("successStep", jobRepository)
            .tasklet(successTasklet(), transactionManager)
            .build()
    }

    @Bean
    fun failureStep(jobRepository: JobRepository, transactionManager: PlatformTransactionManager): Step {
        return StepBuilder("failureStep", jobRepository)
            .tasklet(failTasklet(), transactionManager)
            .build()
    }


    @Bean
    fun successTasklet(): Tasklet {
        return Tasklet { _, _ ->
            println("Success!")
            RepeatStatus.FINISHED
        }
    }

    @Bean
    fun failTasklet(): Tasklet {
        return Tasklet { _, _ ->
            println("Failure!")
            RepeatStatus.FINISHED
        }
    }


    @Bean
    fun passTasklet(): Tasklet {
        return Tasklet { _, _ ->
            // 테스트를 위해 의도적으로 예외를 발생시켜 FAILED 상태를 유도합니다.
            throw RuntimeException("Causing a failure")
            // 정상 종료 테스트 시 아래 주석을 해제하세요.
            // RepeatStatus.FINISHED
        }
    }

    @Bean
    fun decider(): JobExecutionDecider {
        // 자바 코드 하단에 선언되어 있던 커스텀 디사이더 빈 반환
        return RandomDecider()
    }
}

fun main(args: Array<String>) {
    runApplication<ConditionalJob>(*args)
}