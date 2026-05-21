package com.revy.example.scheduler.quartz.impl;

import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.scheduler.quartz.QuartzCommand;
import lombok.RequiredArgsConstructor;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuartzCommandImpl implements QuartzCommand {

    private final Scheduler scheduler;

    @Override
    public void triggerJob(String group, String name) {
        wrap(() -> {
            JobKey key = JobKey.jobKey(name, group);
            if (!scheduler.checkExists(key)) throw new BusinessException(ErrorCode.SCHEDULER_JOB_NOT_FOUND);
            scheduler.triggerJob(key);
        });
    }

    @Override
    public void pauseJob(String group, String name) {
        wrap(() -> {
            JobKey key = JobKey.jobKey(name, group);
            if (!scheduler.checkExists(key)) throw new BusinessException(ErrorCode.SCHEDULER_JOB_NOT_FOUND);
            scheduler.pauseJob(key);
        });
    }

    @Override
    public void resumeJob(String group, String name) {
        wrap(() -> {
            JobKey key = JobKey.jobKey(name, group);
            if (!scheduler.checkExists(key)) throw new BusinessException(ErrorCode.SCHEDULER_JOB_NOT_FOUND);
            scheduler.resumeJob(key);
        });
    }

    @Override
    public void pauseTrigger(String group, String name) {
        wrap(() -> {
            TriggerKey key = TriggerKey.triggerKey(name, group);
            if (!scheduler.checkExists(key)) throw new BusinessException(ErrorCode.SCHEDULER_TRIGGER_NOT_FOUND);
            scheduler.pauseTrigger(key);
        });
    }

    @Override
    public void resumeTrigger(String group, String name) {
        wrap(() -> {
            TriggerKey key = TriggerKey.triggerKey(name, group);
            if (!scheduler.checkExists(key)) throw new BusinessException(ErrorCode.SCHEDULER_TRIGGER_NOT_FOUND);
            scheduler.resumeTrigger(key);
        });
    }

    @Override
    public void deleteJob(String group, String name) {
        wrap(() -> {
            JobKey key = JobKey.jobKey(name, group);
            if (!scheduler.checkExists(key)) throw new BusinessException(ErrorCode.SCHEDULER_JOB_NOT_FOUND);
            scheduler.deleteJob(key);
        });
    }

    @Override
    public void pauseAll() {
        wrap(scheduler::pauseAll);
    }

    @Override
    public void resumeAll() {
        wrap(scheduler::resumeAll);
    }

    // ─────────────────────────────────────────────────────────────

    @FunctionalInterface
    private interface SchedulerAction {
        void execute() throws SchedulerException;
    }

    private void wrap(SchedulerAction action) {
        try {
            action.execute();
        } catch (BusinessException e) {
            throw e;
        } catch (SchedulerException e) {
            throw new BusinessException(ErrorCode.SCHEDULER_OPERATION_FAILED, e.getMessage());
        }
    }
}
