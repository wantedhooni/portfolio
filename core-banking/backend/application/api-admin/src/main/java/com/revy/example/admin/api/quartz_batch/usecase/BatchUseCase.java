package com.revy.example.admin.api.quartz_batch.usecase;

import com.revy.example.business.batch.dto.BatchJobExecutionDetailDto;
import com.revy.example.business.batch.dto.BatchJobExecutionDto;
import com.revy.example.business.batch.dto.BatchSummaryDto;

import java.util.List;

public interface BatchUseCase {
    BatchSummaryDto getSummary();

    List<String> getJobNames();

    List<BatchJobExecutionDto> getExecutions(String jobName, String status, int limit);

    BatchJobExecutionDetailDto getExecutionDetail(Long id);
}
