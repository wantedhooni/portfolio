package com.revy.example.quartz.batch.reader;


import com.revy.example.quartz.batch.dto.BatchJobExecutionDetailDto;
import com.revy.example.quartz.batch.dto.BatchJobExecutionDto;
import com.revy.example.quartz.batch.dto.BatchSummaryDto;

import java.util.List;

/**
 * Spring Batch 메타데이터 테이블(BATCH_JOB_EXECUTION 등)을 조회하는 Reader.
 */
public interface BatchJobReader {

    /** 대시보드 상단 집계. */
    BatchSummaryDto getSummary();

    /** 등록된 잡 이름 목록. */
    List<String> getJobNames();

    /**
     * 최근 잡 실행 목록.
     *
     * @param jobName 잡 이름 필터(nullable)
     * @param status  실행 상태 필터(nullable)
     * @param limit   최대 건수
     */
    List<BatchJobExecutionDto> getExecutions(String jobName, String status, int limit);

    /** 잡 실행 상세(스텝 포함). 없으면 null. */
    BatchJobExecutionDetailDto getExecutionDetail(Long jobExecutionId);
}
