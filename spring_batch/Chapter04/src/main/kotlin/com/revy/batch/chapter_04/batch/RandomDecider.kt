package com.revy.batch.chapter_04.batch

import com.revy.batch.chapter_04.common.log
import org.springframework.batch.core.job.JobExecution
import org.springframework.batch.core.job.flow.FlowExecutionStatus
import org.springframework.batch.core.job.flow.JobExecutionDecider
import org.springframework.batch.core.step.StepExecution
import java.util.Random

class RandomDecider : JobExecutionDecider {
    var random: Random = Random()

    override fun decide(
        jobExecution: JobExecution?,
        stepExecution: StepExecution?
    ): FlowExecutionStatus? {
        return if (random.nextBoolean()) {
            FlowExecutionStatus.COMPLETED
        } else {
            FlowExecutionStatus.FAILED
        }
    }
}