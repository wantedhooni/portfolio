package com.revy.batch.chapter06.quartz

import org.quartz.JobExecutionContext
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.launch.JobOperator
import org.springframework.boot.batch.autoconfigure.BatchProperties
import org.springframework.scheduling.quartz.QuartzJobBean
import org.springframework.stereotype.Component


@Component
class BatchScheduledJob(
    private val job: Job,
    private val jobOperator: JobOperator,
) : QuartzJobBean() {

    override fun executeInternal(context: JobExecutionContext) {
        try {
            jobOperator.startNextInstance(job)
        } catch (e: Exception) {
            throw RuntimeException("Failed to execute batch job: ${job.name}", e)
        }
    }

}