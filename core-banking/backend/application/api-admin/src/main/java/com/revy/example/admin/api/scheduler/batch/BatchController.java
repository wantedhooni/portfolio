package com.revy.example.admin.api.scheduler.batch;

import com.revy.example.admin.api.common.ApiConstants;
import com.revy.example.admin.api.scheduler.batch.payload.BatchPayload;
import com.revy.example.admin.api.scheduler.batch.usecase.BatchUseCase;
import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.core.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/scheduler/batch")
@RequiredArgsConstructor
public class BatchController {

    private final BatchUseCase useCase;

    // ── Read ─────────────────────────────────────────────────────

    @GetMapping("/jobs")
    @PreAuthorize("hasAuthority('SCHEDULER_READ')")
    public ApiResponse<List<BatchPayload.JobResponse>> listJobs() {
        return ApiResponse.ok(useCase.listJobs());
    }

    @GetMapping("/jobs/{jobName}/instances")
    @PreAuthorize("hasAuthority('SCHEDULER_READ')")
    public ApiPageResponse<BatchPayload.InstanceResponse> instances(@PathVariable String jobName,
                                                                    Pageable pageable) {
        PageImpl<BatchPayload.InstanceResponse> page = useCase.searchInstances(jobName, pageable);
        return ApiPageResponse.of(page.getContent(), page.getTotalElements(), page.getNumber(), page.getSize());
    }

    @GetMapping("/executions")
    @PreAuthorize("hasAuthority('SCHEDULER_READ')")
    public ApiPageResponse<BatchPayload.ExecutionResponse> executions(
            Pageable pageable,
            @RequestParam(required = false) String jobName,
            @RequestParam(required = false) String status) {
        PageImpl<BatchPayload.ExecutionResponse> page = useCase.searchExecutions(jobName, status, pageable);
        return ApiPageResponse.of(page.getContent(), page.getTotalElements(), page.getNumber(), page.getSize());
    }

    @GetMapping("/executions/{id}")
    @PreAuthorize("hasAuthority('SCHEDULER_READ')")
    public ApiResponse<BatchPayload.ExecutionResponse> execution(@PathVariable Long id) {
        return ApiResponse.ok(useCase.getExecution(id));
    }

    @GetMapping("/running")
    @PreAuthorize("hasAuthority('SCHEDULER_READ')")
    public ApiResponse<List<BatchPayload.ExecutionResponse>> running(
            @RequestParam(required = false) String jobName) {
        return ApiResponse.ok(useCase.listRunning(jobName));
    }

    // ── Control ──────────────────────────────────────────────────

    /**
     * 즉시 실행. CONTROL 노드(api-admin)에서는 {@code BATCH_LAUNCH_NOT_SUPPORTED} 반환.
     * 워커 노드용 / 또는 별도 통합 환경에서 사용.
     */
    @PostMapping("/launch")
    @PreAuthorize("hasAuthority('SCHEDULER_WRITE')")
    public ApiResponse<Long> launch(@RequestBody @Valid BatchPayload.LaunchRequest req) {
        return ApiResponse.ok(useCase.launch(req));
    }

    /**
     * 워커가 폴링하는 실행 요청 큐에 INSERT. CONTROL 노드에서 권장.
     */
    @PostMapping("/launch-request")
    @PreAuthorize("hasAuthority('SCHEDULER_WRITE')")
    public ApiResponse<Long> requestLaunch(@RequestBody @Valid BatchPayload.LaunchRequest req,
                                           @AuthenticationPrincipal Object principal) {
        String requestedBy = (principal == null) ? "system" : principal.toString();
        return ApiResponse.ok(useCase.requestLaunch(req, requestedBy));
    }

    @PostMapping("/executions/{id}/stop")
    @PreAuthorize("hasAuthority('SCHEDULER_WRITE')")
    public ApiResponse<Void> stop(@PathVariable Long id) {
        useCase.stop(id);
        return ApiResponse.ok();
    }

    @PostMapping("/executions/{id}/abandon")
    @PreAuthorize("hasAuthority('SCHEDULER_WRITE')")
    public ApiResponse<Void> abandon(@PathVariable Long id) {
        useCase.abandon(id);
        return ApiResponse.ok();
    }

    @PostMapping("/executions/{id}/restart")
    @PreAuthorize("hasAuthority('SCHEDULER_WRITE')")
    public ApiResponse<Long> restart(@PathVariable Long id) {
        return ApiResponse.ok(useCase.restart(id));
    }
}
