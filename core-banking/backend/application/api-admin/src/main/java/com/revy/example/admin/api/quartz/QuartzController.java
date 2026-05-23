package com.revy.example.admin.api.quartz;

import com.revy.example.admin.api.common.ApiConstants;
import com.revy.example.admin.api.quartz.payload.QuartzPayload;
import com.revy.example.admin.api.quartz.usecase.QuartzUseCase;
import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.core.common.ApiResponse;
import com.revy.example.quartz.enums.QuartzJobExecutionStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Quartz 제어 / 이력 조회 REST API. {@code module/quartz-batch} 를 호출한다.
 *
 * <p>SCHEDULER_READ - 조회<br/>
 * SCHEDULER_WRITE - 등록·수정·삭제·제어 (pause/resume/runNow)</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/quartz")
public class QuartzController {

    private final QuartzUseCase useCase;

    // ── 조회 ────────────────────────────────────────────────────

    @GetMapping("/jobs")
    @PreAuthorize("hasAuthority('SCHEDULER_READ')")
    public ApiResponse<List<QuartzPayload.JobResponse>> listJobs() {
        return ApiResponse.ok(useCase.listJobs());
    }

    @GetMapping("/jobs/running")
    @PreAuthorize("hasAuthority('SCHEDULER_READ')")
    public ApiResponse<List<QuartzPayload.RunningJobResponse>> listRunning() {
        return ApiResponse.ok(useCase.listRunningJobs());
    }

    /**
     * 실행 이력 페이지.
     * - jobGroup+jobName 지정 시 해당 Job 의 이력
     * - status 지정 시 상태별 이력
     * - 둘 다 지정 안 하면 400
     */
    @GetMapping("/history")
    @PreAuthorize("hasAuthority('SCHEDULER_READ')")
    public ApiResponse<ApiPageResponse<QuartzPayload.HistoryResponse>> history(
            Pageable pageable,
            @RequestParam(required = false) String jobGroup,
            @RequestParam(required = false) String jobName,
            @RequestParam(required = false) QuartzJobExecutionStatus status
    ) {
        if (jobGroup != null && !jobGroup.isBlank() && jobName != null && !jobName.isBlank()) {
            return ApiResponse.ok(useCase.searchHistoryByJob(jobGroup, jobName, pageable));
        }
        if (status != null) {
            return ApiResponse.ok(useCase.searchHistoryByStatus(status, pageable));
        }
        // 기본값: RUNNING 상태부터 (전체 페이지 부담 완화)
        return ApiResponse.ok(useCase.searchHistoryByStatus(QuartzJobExecutionStatus.RUNNING, pageable));
    }

    /**
     * 완료 이력(SUCCESS·FAILED·VETOED) 페이지.
     * jobName 파라미터로 부분 일치 필터 가능.
     */
    @GetMapping("/history/completed")
    @PreAuthorize("hasAuthority('SCHEDULER_READ')")
    public ApiResponse<ApiPageResponse<QuartzPayload.HistoryResponse>> historyCompleted(
            Pageable pageable,
            @RequestParam(required = false) String jobName
    ) {
        return ApiResponse.ok(useCase.searchHistoryCompleted(jobName, pageable));
    }

    // ── 등록/수정/삭제 ───────────────────────────────────────────

    @PostMapping("/jobs")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SCHEDULER_WRITE')")
    public ApiResponse<Void> create(@Valid @RequestBody QuartzPayload.UpsertRequest req) {
        useCase.createJob(req);
        return ApiResponse.ok();
    }

    /** Job 전체 교체(JobDataMap·jobType 변경 포함). 내부적으로 delete + create */
    @PutMapping("/jobs/{group}/{name}")
    @PreAuthorize("hasAuthority('SCHEDULER_WRITE')")
    public ApiResponse<Void> update(@PathVariable String group,
                                    @PathVariable String name,
                                    @Valid @RequestBody QuartzPayload.UpsertRequest req) {
        useCase.updateJob(group, name, req);
        return ApiResponse.ok();
    }

    /** 스케줄(Trigger)만 재설정 - JobDataMap 유지 */
    @PatchMapping("/jobs/{group}/{name}/schedule")
    @PreAuthorize("hasAuthority('SCHEDULER_WRITE')")
    public ApiResponse<Void> reschedule(@PathVariable String group,
                                        @PathVariable String name,
                                        @Valid @RequestBody QuartzPayload.RescheduleRequest req) {
        useCase.reschedule(group, name, req);
        return ApiResponse.ok();
    }

    @DeleteMapping("/jobs/{group}/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('SCHEDULER_WRITE')")
    public void delete(@PathVariable String group, @PathVariable String name) {
        useCase.deleteJob(group, name);
    }

    // ── 제어 ────────────────────────────────────────────────────

    @PostMapping("/jobs/{group}/{name}/pause")
    @PreAuthorize("hasAuthority('SCHEDULER_WRITE')")
    public ApiResponse<Void> pause(@PathVariable String group, @PathVariable String name) {
        useCase.pauseJob(group, name);
        return ApiResponse.ok();
    }

    @PostMapping("/jobs/{group}/{name}/resume")
    @PreAuthorize("hasAuthority('SCHEDULER_WRITE')")
    public ApiResponse<Void> resume(@PathVariable String group, @PathVariable String name) {
        useCase.resumeJob(group, name);
        return ApiResponse.ok();
    }

    @PostMapping("/jobs/{group}/{name}/run")
    @PreAuthorize("hasAuthority('SCHEDULER_WRITE')")
    public ApiResponse<Void> runNow(@PathVariable String group, @PathVariable String name) {
        useCase.runNow(group, name);
        return ApiResponse.ok();
    }

    /**
     * FAILED 실행 이력 ID 로 해당 Job 을 즉시 재실행한다.
     * Job 이 Quartz 에 존재하지 않으면 400 을 반환한다.
     */
    @PostMapping("/history/{id}/retry")
    @PreAuthorize("hasAuthority('SCHEDULER_WRITE')")
    public ApiResponse<Void> retry(@PathVariable Long id) {
        useCase.retryJob(id);
        return ApiResponse.ok();
    }
}
