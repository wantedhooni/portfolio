package com.revy.batch.chapter_04.batch

import com.revy.batch.chapter_04.common.log
import org.springframework.batch.core.converter.JobParametersConversionException
import org.springframework.batch.core.job.parameters.JobParameters
import org.springframework.batch.core.job.parameters.JobParametersValidator

class ParameterValidator : JobParametersValidator {
    override fun validate(parameters: JobParameters?) {

        val fileName = parameters?.getString("fileName")
            ?: throw JobParametersConversionException("fileName parameter is missing")
        log.info { "fileName: ${fileName}" }
        if (!fileName.endsWith("csv")) {
            throw JobParametersConversionException("fileName parameter does not use csv file extension")
        }
    }
}