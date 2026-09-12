package com.revy.batch.chapter_04.batch

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.annotation.AfterStep
import org.springframework.batch.core.annotation.BeforeStep
import org.springframework.batch.core.step.StepExecution

private val log = KotlinLogging.logger {}

class LoggingStepStartStopListener {
    @BeforeStep
    fun beforeStop(stepExecution: StepExecution) {
        log.info { "${stepExecution.getStepName()} has begun!" }
    }

    @AfterStep
    fun afterStep(stepExecution: StepExecution): ExitStatus {
        log.info { "${stepExecution.getStepName()} has ended!" }
        return stepExecution.getExitStatus();
    }
}