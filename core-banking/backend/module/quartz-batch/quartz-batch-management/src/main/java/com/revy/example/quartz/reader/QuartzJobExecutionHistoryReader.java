package com.revy.example.quartz.reader;

import com.revy.example.quartz.domain.QuartzJobExecutionHistory;
import com.revy.example.quartz.dto.QuartzJobExecutionHistoryResult;
import com.revy.example.quartz.enums.QuartzJobExecutionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Quartz 작업 실행 이력 조회를 담당하는 Reader 계층 계약이다.
 */
public interface QuartzJobExecutionHistoryReader {

    /**
     * Quartz fire instance id로 실행 이력 엔티티를 조회한다.
     */
    Optional<QuartzJobExecutionHistory> findByFireInstanceId(String fireInstanceId);
    Optional<QuartzJobExecutionHistory> findById(Long id);
    /**
     * 작업 그룹과 작업명 기준으로 실행 이력 projection 페이지를 조회한다.
     */
    Page<QuartzJobExecutionHistoryResult> findByJob(
        String jobGroup,
        String jobName,
        Pageable pageable
    );

    /**
     * 실행 상태 기준으로 실행 이력 projection 페이지를 조회한다.
     */
    Page<QuartzJobExecutionHistoryResult> findByStatus(
        QuartzJobExecutionStatus status,
        Pageable pageable
    );

    /**
     * 완료 이력(SUCCESS·FAILED·VETOED) 전체 페이지 조회.
     * jobName 이 null 이 아니면 부분 일치(contains) 필터를 적용한다.
     */
    Page<QuartzJobExecutionHistoryResult> findCompleted(String jobName, Pageable pageable);
}
