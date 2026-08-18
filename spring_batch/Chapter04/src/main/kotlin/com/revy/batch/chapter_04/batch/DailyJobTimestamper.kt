package com.revy.batch.chapter_04.batch

import org.springframework.batch.core.job.parameters.JobParameters
import org.springframework.batch.core.job.parameters.JobParametersBuilder
import org.springframework.batch.core.job.parameters.JobParametersIncrementer
import java.util.Date

class DailyJobTimestamper : JobParametersIncrementer {
    override fun getNext(parameters: JobParameters?): JobParameters? {
        return JobParametersBuilder()
            .addDate("currentDate", Date())
            .toJobParameters()
    }
}