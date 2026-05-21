package com.revy.example.scheduler.quartz;

import com.revy.example.scheduler.quartz.dto.QuartzExecutionResult;
import com.revy.example.scheduler.quartz.dto.QuartzJobResult;
import com.revy.example.scheduler.quartz.dto.QuartzTriggerResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface QuartzReader {
    /** 등록된 모든 Job 조회 */
    List<QuartzJobResult> listJobs();

    /** group + name 으로 Job 단건 조회 */
    Optional<QuartzJobResult> findJob(String group, String name);

    /** 모든 Trigger 조회 */
    List<QuartzTriggerResult> listTriggers();

    /**
     * 실행 이력 조회.
     * 워커 노드의 {@code RevyJobHistoryListener} 가 {@code QRTZ_JOB_HISTORY}
     * 테이블에 저장한 레코드를 페이지로 반환.
     */
    Page<QuartzExecutionResult> searchExecutions(Pageable pageable,
                                                 String jobName,
                                                 String result);

    /** 현재 fire 중인 Trigger (cluster) */
    List<QuartzExecutionResult> listCurrentlyExecuting();
}
