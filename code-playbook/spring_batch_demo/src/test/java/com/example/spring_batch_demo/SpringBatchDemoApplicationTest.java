package com.example.spring_batch_demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringBootTest
class MemberBatchTest {

    @Autowired
    private JobOperatorTestUtils jobOperatorTestUtils;

    @Autowired
    private Job memberJob;

    @BeforeEach
    void setUp() {
        jobOperatorTestUtils.setJob(memberJob);
    }

    @Test
    void testMemberBatch() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("name", "memberBatch")
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobOperatorTestUtils.startJob(jobParameters);

        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }
}