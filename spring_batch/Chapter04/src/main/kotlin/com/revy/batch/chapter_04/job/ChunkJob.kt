package com.revy.batch.chapter_04.job

import com.revy.batch.chapter_04.batch.LoggingStepStartStopListener
import com.revy.batch.chapter_04.batch.RandomChunkSizePolicy
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.infrastructure.item.ItemWriter
import org.springframework.batch.infrastructure.item.support.ListItemReader
import org.springframework.batch.infrastructure.repeat.CompletionPolicy
import org.springframework.batch.infrastructure.repeat.policy.CompositeCompletionPolicy
import org.springframework.batch.infrastructure.repeat.policy.SimpleCompletionPolicy
import org.springframework.batch.infrastructure.repeat.policy.TimeoutTerminationPolicy
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

@Configuration
@SpringBootApplication
class ChunkJob {

    @Bean
    fun chunkBasedJob(jobRepository: JobRepository, chunkStep: Step): Job {
        return JobBuilder("chunkBasedJob", jobRepository)
            .start(chunkStep)
            .build()
    }

    @Bean
    fun chunkStep(
        jobRepository: JobRepository,
        itemReader: ListItemReader<String>,
        itemWriter: ItemWriter<String>,
    ): Step {
        return StepBuilder("chunkStep", jobRepository)
            // Batch 6에서는 chunk 설정을 넘길 때 transactionManager를 반드시 함께 매개변수로 지정합니다.
            .chunk<String, String>(1000)
            .reader(itemReader)
            .writer(itemWriter)
            .listener(LoggingStepStartStopListener()) // 구현된 리스너가 있다면 주석을 해제하세요.
            .build()
    }

    @Bean
    fun itemReader(): ListItemReader<String> {
        val items = ArrayList<String>(100000)
        for (i in 0 until 100000) {
            items.add(UUID.randomUUID().toString())
        }
        return ListItemReader(items)
    }

    @Bean
    fun itemWriter(): ItemWriter<String> {
        // SAM 변환을 적용하여 복잡한 익명 인터페이스 선언을 람다식으로 단축했습니다.
        return ItemWriter { items ->
            for (item in items) {
                println(">> current item = $item")
            }
        }
    }

    /**
     * Spring Batch 6 에서는 불필요
     */
    @Bean
    fun completionPolicy(): CompletionPolicy {
        val policy = CompositeCompletionPolicy()
        policy.setPolicies(
            arrayOf(
                TimeoutTerminationPolicy(3),
                SimpleCompletionPolicy(1000)
            )
        )
        return policy
    }


    @Bean
    fun randomCompletionPolicy(): CompletionPolicy {
        // 프로젝트 내부 패키지에 구현되어 있을 커스텀 청크 완료 정책 클래스
        return RandomChunkSizePolicy()
    }
}

fun main(args: Array<String>) {
    runApplication<ChunkJob>(*args)
}