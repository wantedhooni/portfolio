package com.revy.example.admin.api.quartz_batch.usecase.impl;

import com.revy.example.admin.api.quartz_batch.usecase.BatchUseCase;
import com.revy.example.quartz.batch.dto.BatchJobExecutionDetailDto;
import com.revy.example.quartz.batch.dto.BatchJobExecutionDto;
import com.revy.example.quartz.batch.dto.BatchSummaryDto;
import com.revy.example.quartz.batch.reader.BatchJobReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BatchUseCaseImpl implements BatchUseCase {

    private final BatchJobReader batchJobReader;

    @Override
    public BatchSummaryDto getSummary() {
        return batchJobReader.getSummary();
    }

    @Override
    public List<String> getJobNames() {
        return batchJobReader.getJobNames();
    }

    @Override
    public List<BatchJobExecutionDto> getExecutions(String jobName, String status, int limit) {
        return batchJobReader.getExecutions(jobName, status, limit);
    }

    @Override
    public BatchJobExecutionDetailDto getExecutionDetail(Long id) {
        return batchJobReader.getExecutionDetail(id);
    }
}
