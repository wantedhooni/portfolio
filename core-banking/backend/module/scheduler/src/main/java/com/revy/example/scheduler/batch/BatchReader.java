package com.revy.example.scheduler.batch;

import com.revy.example.scheduler.batch.dto.BatchExecutionResult;
import com.revy.example.scheduler.batch.dto.BatchInstanceResult;
import com.revy.example.scheduler.batch.dto.BatchJobResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface BatchReader {
    /** 메타데이터에 존재하는 모든 Job 이름 + 최근 실행 정보 */
    List<BatchJobResult> listJobs();

    /** Job 의 JobInstance 페이지 */
    Page<BatchInstanceResult> searchInstances(String jobName, Pageable pageable);

    /** 단일 Execution 상세 (StepExecution 포함) */
    Optional<BatchExecutionResult> findExecution(Long executionId);

    /** 실행 이력 페이지 (모든 Job) */
    Page<BatchExecutionResult> searchExecutions(String jobName,
                                                String status,
                                                Pageable pageable);

    /** 현재 실행 중인(Running) Execution */
    List<BatchExecutionResult> listRunning(String jobName);
}
