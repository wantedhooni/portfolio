package com.revy.example.admin.api.scheduler.batch.usecase;

import com.revy.example.admin.api.scheduler.batch.payload.BatchPayload;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BatchUseCase {
    List<BatchPayload.JobResponse> listJobs();
    PageImpl<BatchPayload.InstanceResponse> searchInstances(String jobName, Pageable pageable);
    PageImpl<BatchPayload.ExecutionResponse> searchExecutions(String jobName, String status, Pageable pageable);
    BatchPayload.ExecutionResponse getExecution(Long id);
    List<BatchPayload.ExecutionResponse> listRunning(String jobName);

    Long launch(BatchPayload.LaunchRequest req);
    Long requestLaunch(BatchPayload.LaunchRequest req, String requestedBy);
    void stop(Long executionId);
    void abandon(Long executionId);
    Long restart(Long executionId);
}
