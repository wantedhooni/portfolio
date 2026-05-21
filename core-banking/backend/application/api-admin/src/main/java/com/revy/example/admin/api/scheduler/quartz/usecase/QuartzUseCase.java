package com.revy.example.admin.api.scheduler.quartz.usecase;

import com.revy.example.admin.api.scheduler.quartz.payload.QuartzPayload;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QuartzUseCase {
    List<QuartzPayload.JobResponse> listJobs();
    QuartzPayload.JobResponse getJob(String group, String name);
    List<QuartzPayload.TriggerResponse> listTriggers();
    PageImpl<QuartzPayload.ExecutionResponse> searchExecutions(Pageable pageable, String jobName, String result);
    List<QuartzPayload.ExecutionResponse> listRunning();

    void triggerJob(String group, String name);
    void pauseJob(String group, String name);
    void resumeJob(String group, String name);
    void pauseTrigger(String group, String name);
    void resumeTrigger(String group, String name);
    void deleteJob(String group, String name);
    void pauseAll();
    void resumeAll();
}
