package com.revy.batch.chapter_04.job

import jakarta.annotation.PostConstruct
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.SimpleSystemProcessExitCodeMapper
import org.springframework.batch.core.step.tasklet.SystemCommandTasklet
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.transaction.PlatformTransactionManager
import java.nio.file.Files
import java.nio.file.Paths

@Configuration
@SpringBootApplication
class AdvancedSystemCommandJob {

    companion object{
        val workingDir = Paths.get(
            System.getProperty("user.home"),
            "tem",
            "spring-batch"
        )
    }

    @PostConstruct
    fun init(){
        Files.createDirectories(workingDir)
    }


    @Bean
    fun touchCodeMapper(): SimpleSystemProcessExitCodeMapper =
        SimpleSystemProcessExitCodeMapper()

    @Bean
    fun job(
        jobRepository: JobRepository,
        systemCommandStep: Step
    ): Job {
        return JobBuilder("systemCommandJob", jobRepository)
            .start { systemCommandStep }
            .build()
    }

    @Bean
    fun systemCommandStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        systemCommandTasklet: SystemCommandTasklet,
    ): Step{
        return StepBuilder("systemCommandStep", jobRepository)
            .tasklet(systemCommandTasklet, transactionManager)
            .build()
    }


    @Bean
    fun systemCommandTasklet(
        touchCodeMapper: SimpleSystemProcessExitCodeMapper,
    ): SystemCommandTasklet {
        var tasklet = SystemCommandTasklet()
        tasklet.setCommand("touch tmp.txt")
        tasklet.setTimeout(5000)
        tasklet.setInterruptOnCancel(true)
//        tasklet.setWorkingDirectory("/Users/mminella/spring-batch")
        tasklet.setWorkingDirectory(workingDir.toString())
        tasklet.setSystemProcessExitCodeMapper(touchCodeMapper)
        tasklet.setTerminationCheckInterval(5000)
        tasklet.setTaskExecutor(SimpleAsyncTaskExecutor())
        return tasklet
    }
}

fun main(args: Array<String>) {
    runApplication<AdvancedSystemCommandJob>(*args)
}