package com.example.spring_batch_demo;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class MemberBatchConfiguration {

    @Bean
    public Step memberStep(JobRepository jobRepository,
                           PlatformTransactionManager transactionManager,
                           ItemReader<Member> memberItemReader,
                           MemberItemProcessor memberItemProcessor,
                           MemberItemWriter memberItemWriter) {

        return new StepBuilder("memberStep", jobRepository)
                .<Member, Member>chunk(10)
                .transactionManager(transactionManager)
                .reader(memberItemReader)
                .processor(memberItemProcessor)
                .writer(memberItemWriter)
                .build();
    }

    @Bean
    public Job memberJob(JobRepository jobRepository, Step memberStep) {

        return new JobBuilder("memberJob", jobRepository).start(memberStep).build();
    }

}
