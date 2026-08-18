package com.revy.batch.chapter_04.batch

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.core.annotation.BeforeJob
import org.springframework.batch.core.job.JobExecution

private val log = KotlinLogging.logger {}

class JobLoggerListener {
    companion object {
        const val START_MESSAGE = "%s is beginning execution"
        const val END_MESSAGE = "%s ends execution. status:%s"
    }

    @BeforeJob
    fun beforeJob(jobExecution: JobExecution) {
        log.info { START_MESSAGE.format(jobExecution.jobInstance.jobName) }
    }

    fun afterJob(jobExecution: JobExecution) {
        log.info { END_MESSAGE.format(jobExecution.jobInstance.jobName, jobExecution.status) }
    }


}