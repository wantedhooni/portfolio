package com.revy.batch.chapter_04.batch



import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.StepContribution
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.infrastructure.item.ExecutionContext
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Value

private val log = KotlinLogging.logger {}

class HelloWorldTasklet : Tasklet {

    companion object{
        @Value("#{jobParameters['name']}")
        private val name: String? = null
    }

    override fun execute(
        contribution: StepContribution,
        chunkContext: ChunkContext
    ): RepeatStatus? {

        val jobExecutionContext : ExecutionContext = chunkContext.stepContext.stepExecution.executionContext
		jobExecutionContext.put("user.name", name);
        println("HELLO WORLD ${name}")
        log.info { "HELLO WORLD ${name}" }
        return RepeatStatus.FINISHED;
    }
}