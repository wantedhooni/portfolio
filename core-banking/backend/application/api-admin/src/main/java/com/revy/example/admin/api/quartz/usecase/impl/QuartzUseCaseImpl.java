package com.revy.example.admin.api.quartz.usecase.impl;

import com.revy.example.admin.api.quartz.payload.QuartzPayload;
import com.revy.example.admin.api.quartz.usecase.QuartzUseCase;
import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.quartz.dto.QuartzJobExecutionHistoryResult;
import com.revy.example.quartz.dto.QuartzJobResult;
import com.revy.example.quartz.dto.QuartzJobUpsertCommand;
import com.revy.example.quartz.dto.QuartzRunningJobResult;
import com.revy.example.quartz.enums.QuartzJobExecutionStatus;
import com.revy.example.quartz.handler.QuartzJobHandler;
import com.revy.example.quartz.reader.QuartzJobExecutionHistoryReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuartzUseCaseImpl implements QuartzUseCase {

    private final QuartzJobHandler jobHandler;
    private final QuartzJobExecutionHistoryReader historyReader;

    // ── 조회 ────────────────────────────────────────────────────

    @Override
    public List<QuartzPayload.JobResponse> listJobs() {
        return jobHandler.findAllJobs()
            .stream()
            .map(this::toJob)
            .toList();
    }

    @Override
    public List<QuartzPayload.RunningJobResponse> listRunningJobs() {
        return jobHandler.findRunningJobs()
            .stream()
            .map(this::toRunning)
            .toList();
    }

    @Override
    public ApiPageResponse<QuartzPayload.HistoryResponse> searchHistoryByJob(String jobGroup, String jobName,
                                                                             Pageable pageable) {
        Page<QuartzJobExecutionHistoryResult> page = historyReader.findByJob(jobGroup, jobName, pageable);
        return ApiPageResponse.of(page.getContent()
                                      .stream()
                                      .map(this::toHistory)
                                      .toList(), page.getTotalElements(), page.getNumber(), page.getSize());
    }

    @Override
    public ApiPageResponse<QuartzPayload.HistoryResponse> searchHistoryByStatus(QuartzJobExecutionStatus status,
                                                                                Pageable pageable) {
        Page<QuartzJobExecutionHistoryResult> page = historyReader.findByStatus(status, pageable);
        return ApiPageResponse.of(page.getContent()
                                      .stream()
                                      .map(this::toHistory)
                                      .toList(), page.getTotalElements(), page.getNumber(), page.getSize());
    }

    @Override
    public ApiPageResponse<QuartzPayload.HistoryResponse> searchHistoryCompleted(String jobName, Pageable pageable) {
        Page<QuartzJobExecutionHistoryResult> page = historyReader.findCompleted(jobName, pageable);
        return ApiPageResponse.of(page.getContent()
                                      .stream()
                                      .map(this::toHistory)
                                      .toList(), page.getTotalElements(), page.getNumber(), page.getSize());
    }

    // ── 등록/수정/삭제 ───────────────────────────────────────────

    @Override
    public void createJob(QuartzPayload.UpsertRequest req) {
        jobHandler.createJob(toCommand(req.jobName(), req.jobGroup(), req));
    }

    @Override
    public void updateJob(String jobGroup, String jobName, QuartzPayload.UpsertRequest req) {
        jobHandler.updateJob(jobGroup, jobName, toCommand(jobName, jobGroup, req));
    }

    @Override
    public void reschedule(String jobGroup, String jobName, QuartzPayload.RescheduleRequest req) {
        QuartzJobUpsertCommand cmd = new QuartzJobUpsertCommand(jobName,
                                                                jobGroup,
                                                                null,
                                                                req.scheduleType(),
                                                                req.cronExpression(),
                                                                req.repeatIntervalMs(),
                                                                req.repeatCount(),
                                                                req.startAt(),
                                                                null,
                                                                null);
        jobHandler.updateScheduleOnly(jobGroup, jobName, cmd);
    }

    @Override
    public void deleteJob(String jobGroup, String jobName) {
        jobHandler.deleteJob(jobGroup, jobName);
    }

    // ── 제어 ────────────────────────────────────────────────────

    @Override
    public void pauseJob(String jobGroup, String jobName) {
        jobHandler.pauseJob(jobGroup, jobName);
    }

    @Override
    public void resumeJob(String jobGroup, String jobName) {
        jobHandler.resumeJob(jobGroup, jobName);
    }

    @Override
    public void runNow(String jobGroup, String jobName) {
        jobHandler.runNow(jobGroup, jobName);
    }

    @Override
    public void retryJob(Long historyId) {
        jobHandler.retry(historyId);
    }
    // ── private ─────────────────────────────────────────────────

    private QuartzJobUpsertCommand toCommand(String name, String group, QuartzPayload.UpsertRequest r) {
        return new QuartzJobUpsertCommand(name,
                                          group,
                                          r.jobType(),
                                          r.scheduleType(),
                                          r.cronExpression(),
                                          r.repeatIntervalMs(),
                                          r.repeatCount(),
                                          r.startAt(),
                                          r.description(),
                                          r.jobData());
    }

    private QuartzPayload.JobResponse toJob(QuartzJobResult r) {
        return new QuartzPayload.JobResponse(r.jobName(),
                                             r.jobGroup(),
                                             r.triggerName(),
                                             r.triggerGroup(),
                                             r.triggerState(),
                                             r.triggerType(),
                                             r.description(),
                                             r.previousFireTime(),
                                             r.nextFireTime());
    }

    private QuartzPayload.RunningJobResponse toRunning(QuartzRunningJobResult r) {
        return new QuartzPayload.RunningJobResponse(r.jobName(),
                                                    r.jobGroup(),
                                                    r.triggerName(),
                                                    r.triggerGroup(),
                                                    r.fireTime(),
                                                    r.scheduledFireTime(),
                                                    r.runningTimeMs());
    }

    private QuartzPayload.HistoryResponse toHistory(QuartzJobExecutionHistoryResult r) {
        return new QuartzPayload.HistoryResponse(r.id(),
                                                 r.schedulerName(),
                                                 r.fireInstanceId(),
                                                 r.jobName(),
                                                 r.jobGroup(),
                                                 r.triggerName(),
                                                 r.triggerGroup(),
                                                 r.status(),
                                                 r.scheduledFireTime(),
                                                 r.fireTime(),
                                                 r.endTime(),
                                                 r.durationMs(),
                                                 r.errorMessage());
    }
}
