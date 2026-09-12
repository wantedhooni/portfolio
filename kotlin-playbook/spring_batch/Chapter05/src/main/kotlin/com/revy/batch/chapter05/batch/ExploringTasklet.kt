package com.revy.batch.chapter05

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.core.job.JobInstance
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.StepContribution
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.context.annotation.Configuration

private val log = KotlinLogging.logger {}

class ExploringTasklet(
    val jobRepository: JobRepository
) : Tasklet {
    override fun execute(
        contribution: StepContribution, chunkContext: ChunkContext
    ): RepeatStatus? {
        val jobName = chunkContext.stepContext.jobName;
        val instanceList: List<JobInstance> = jobRepository.findJobInstances(jobName)
        log.info { "There are ${jobName} job instances for the job ${instanceList}" }
        log.info { "They have had the following results" }
        log.info { "************************************" }
        instanceList.forEach { instance ->
            val jobExecutions = jobRepository.getJobExecutions(instance)

            log.info { "Instance %d had ${jobExecutions} executions" }

            jobExecutions.forEach { execution ->
                log.info { "Execution %d resulted in Exit Status ${execution}" }
            }
        }
        return RepeatStatus.FINISHED;
    }
}