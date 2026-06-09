package com.revy.example.quartz.batch.dto;


import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * JOB_EXECUTION 상세 - 실행 요약 + 잡 파라미터 + 스텝 실행 목록.
 */
@Getter
@Builder
public class BatchJobExecutionDetailDto {
    private final BatchJobExecutionDto execution;
    private final String exitMessage;
    private final Map<String, String> jobParameters;
    private final List<BatchStepExecutionDto> steps;
}
