package com.revy.example.admin.api.scheduler.quartz;

import com.revy.example.admin.api.common.ApiConstants;
import com.revy.example.admin.api.scheduler.quartz.payload.QuartzPayload;
import com.revy.example.admin.api.scheduler.quartz.usecase.QuartzUseCase;
import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.core.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/scheduler/quartz")
@RequiredArgsConstructor
public class QuartzController {

    private final QuartzUseCase useCase;

    // ── Read ─────────────────────────────────────────────────────

    @GetMapping("/jobs")
    @PreAuthorize("hasAuthority('SCHEDULER_READ')")
    public ApiResponse<List<QuartzPayload.JobResponse>> listJobs() {
        return ApiResponse.ok(useCase.listJobs());
    }

    @GetMapping("/jobs/{group}/{name}")
    @PreAuthorize("hasAuthority('SCHEDULER_READ')")
    public ApiResponse<QuartzPayload.JobResponse> getJob(@PathVariable String group,
                                                         @PathVariable String name) {
        return ApiResponse.ok(useCase.getJob(group, name));
    }

    @GetMapping("/triggers")
    @PreAuthorize("hasAuthority('SCHEDULER_READ')")
    public ApiResponse<List<QuartzPayload.TriggerResponse>> listTriggers() {
        return ApiResponse.ok(useCase.listTriggers());
    }

    @GetMapping("/executions")
    @PreAuthorize("hasAuthority('SCHEDULER_READ')")
    public ApiPageResponse<QuartzPayload.ExecutionResponse> searchExecutions(
            Pageable pageable,
            @RequestParam(required = false) String jobName,
            @RequestParam(required = false) String result) {
        PageImpl<QuartzPayload.ExecutionResponse> page = useCase.searchExecutions(pageable, jobName, result);
        return ApiPageResponse.of(page.getContent(), page.getTotalElements(), page.getNumber(), page.getSize());
    }

    @GetMapping("/running")
    @PreAuthorize("hasAuthority('SCHEDULER_READ')")
    public ApiResponse<List<QuartzPayload.ExecutionResponse>> listRunning() {
        return ApiResponse.ok(useCase.listRunning());
    }

    // ── Control ──────────────────────────────────────────────────

    @PostMapping("/jobs/{group}/{name}/trigger")
    @PreAuthorize("hasAuthority('SCHEDULER_WRITE')")
    public ApiResponse<Void> triggerJob(@PathVariable String group, @PathVariable String name) {
        useCase.triggerJob(group, name);
        return ApiResponse.ok();
    }

    @PostMapping("/jobs/{group}/{name}/pause")
    @PreAuthorize("hasAuthority('SCHEDULER_WRITE')")
    public ApiResponse<Void> pauseJob(@PathVariable String group, @PathVariable String name) {
        useCase.pauseJob(group, name);
        return ApiResponse.ok();
    }

    @PostMapping("/jobs/{group}/{name}/resume")
    @PreAuthorize("hasAuthority('SCHEDULER_WRITE')")
    public ApiResponse<Void> resumeJob(@PathVariable String group, @PathVariable String name) {
        useCase.resumeJob(group, name);
        return ApiResponse.ok();
    }

    @PostMapping("/triggers/{group}/{name}/pause")
    @PreAuthorize("hasAuthority('SCHEDULER_WRITE')")
    public ApiResponse<Void> pauseTrigger(@PathVariable String group, @PathVariable String name) {
        useCase.pauseTrigger(group, name);
        return ApiResponse.ok();
    }

    @PostMapping("/triggers/{group}/{name}/resume")
    @PreAuthorize("hasAuthority('SCHEDULER_WRITE')")
    public ApiResponse<Void> resumeTrigger(@PathVariable String group, @PathVariable String name) {
        useCase.resumeTrigger(group, name);
        return ApiResponse.ok();
    }

    @DeleteMapping("/jobs/{group}/{name}")
    @PreAuthorize("hasAuthority('SCHEDULER_WRITE')")
    public ApiResponse<Void> deleteJob(@PathVariable String group, @PathVariable String name) {
        useCase.deleteJob(group, name);
        return ApiResponse.ok();
    }

    @PostMapping("/pause-all")
    @PreAuthorize("hasAuthority('SCHEDULER_WRITE')")
    public ApiResponse<Void> pauseAll() { useCase.pauseAll(); return ApiResponse.ok(); }

    @PostMapping("/resume-all")
    @PreAuthorize("hasAuthority('SCHEDULER_WRITE')")
    public ApiResponse<Void> resumeAll() { useCase.resumeAll(); return ApiResponse.ok(); }
}
