package com.revy.example.admin.api.quartz.usecase;

import com.revy.example.admin.api.quartz.payload.QuartzPayload;
import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.quartz.enums.QuartzJobExecutionStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QuartzUseCase {

    // ── 조회 ────────────────────────────────────────────────────
    List<QuartzPayload.JobResponse> listJobs();

    List<QuartzPayload.RunningJobResponse> listRunningJobs();

    ApiPageResponse<QuartzPayload.HistoryResponse> searchHistoryByJob(
            String jobGroup, String jobName, Pageable pageable);

    ApiPageResponse<QuartzPayload.HistoryResponse> searchHistoryByStatus(
            QuartzJobExecutionStatus status, Pageable pageable);

    /** SUCCESS·FAILED·VETOED 전체 완료 이력. jobName 은 선택 부분 일치 필터 */
    ApiPageResponse<QuartzPayload.HistoryResponse> searchHistoryCompleted(
            String jobName, Pageable pageable);

    // ── 등록/수정/삭제 ───────────────────────────────────────────
    void createJob(QuartzPayload.UpsertRequest req);

    void updateJob(String jobGroup, String jobName, QuartzPayload.UpsertRequest req);

    void reschedule(String jobGroup, String jobName, QuartzPayload.RescheduleRequest req);

    void deleteJob(String jobGroup, String jobName);

    // ── 제어 ────────────────────────────────────────────────────
    void pauseJob(String jobGroup, String jobName);

    void resumeJob(String jobGroup, String jobName);

    void runNow(String jobGroup, String jobName);

    /** FAILED 실행 이력 ID 로 해당 Job 을 즉시 재실행 */
    void retryJob(Long historyId);
}
