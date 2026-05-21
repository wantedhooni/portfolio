package com.revy.example.admin.api.scheduler.quartz.usecase.impl;

import com.revy.example.admin.api.scheduler.quartz.payload.QuartzPayload;
import com.revy.example.admin.api.scheduler.quartz.usecase.QuartzUseCase;
import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.scheduler.quartz.QuartzCommand;
import com.revy.example.scheduler.quartz.QuartzReader;
import com.revy.example.scheduler.quartz.dto.QuartzExecutionResult;
import com.revy.example.scheduler.quartz.dto.QuartzJobResult;
import com.revy.example.scheduler.quartz.dto.QuartzTriggerResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class QuartzUseCaseImpl implements QuartzUseCase {

    private final QuartzReader  reader;
    private final QuartzCommand command;

    @Override
    public List<QuartzPayload.JobResponse> listJobs() {
        return reader.listJobs().stream().map(this::toJob).toList();
    }

    @Override
    public QuartzPayload.JobResponse getJob(String group, String name) {
        return toJob(reader.findJob(group, name)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULER_JOB_NOT_FOUND)));
    }

    @Override
    public List<QuartzPayload.TriggerResponse> listTriggers() {
        return reader.listTriggers().stream().map(this::toTrigger).toList();
    }

    @Override
    public PageImpl<QuartzPayload.ExecutionResponse> searchExecutions(Pageable pageable, String jobName, String result) {
        Page<QuartzExecutionResult> page = reader.searchExecutions(pageable, jobName, result);
        return new PageImpl<>(page.getContent().stream().map(this::toExecution).toList(),
                pageable, page.getTotalElements());
    }

    @Override
    public List<QuartzPayload.ExecutionResponse> listRunning() {
        return reader.listCurrentlyExecuting().stream().map(this::toExecution).toList();
    }

    @Override public void triggerJob(String group, String name)    { command.triggerJob(group, name); }
    @Override public void pauseJob(String group, String name)      { command.pauseJob(group, name); }
    @Override public void resumeJob(String group, String name)     { command.resumeJob(group, name); }
    @Override public void pauseTrigger(String group, String name)  { command.pauseTrigger(group, name); }
    @Override public void resumeTrigger(String group, String name) { command.resumeTrigger(group, name); }
    @Override public void deleteJob(String group, String name)     { command.deleteJob(group, name); }
    @Override public void pauseAll()  { command.pauseAll(); }
    @Override public void resumeAll() { command.resumeAll(); }

    // ─────────────────────────────────────────────────────────────

    private QuartzPayload.JobResponse toJob(QuartzJobResult r) {
        return new QuartzPayload.JobResponse(
                r.name(), r.group(), r.description(), r.jobClass(),
                r.durable(), r.concurrentExecutionDisallowed(),
                r.persistJobDataAfterExecution(), r.requestsRecovery(),
                r.jobDataMap(),
                r.triggers().stream().map(this::toTrigger).toList()
        );
    }

    private QuartzPayload.TriggerResponse toTrigger(QuartzTriggerResult t) {
        return new QuartzPayload.TriggerResponse(
                t.name(), t.group(), t.jobName(), t.jobGroup(), t.description(),
                t.type(), t.cronExpression(), t.repeatInterval(), t.repeatCount(),
                t.nextFireTime(), t.previousFireTime(), t.startTime(), t.endTime(),
                t.state()
        );
    }

    private QuartzPayload.ExecutionResponse toExecution(QuartzExecutionResult e) {
        return new QuartzPayload.ExecutionResponse(
                e.id(), e.jobName(), e.jobGroup(), e.triggerName(), e.triggerGroup(),
                e.firedAt(), e.completedAt(), e.runTimeMs(), e.result(),
                e.exceptionMessage(), e.instanceId()
        );
    }
}
