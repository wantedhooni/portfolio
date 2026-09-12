package com.revy.springbatchquartz.monitoring;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 배치 실행 이력과 쿼츠 트리거 상태를 조회하는 REST 컨트롤러.
 */
@RestController
@RequestMapping("/api")
public class BatchMonitoringController {

    private final JobExplorer jobExplorer;
    private final Scheduler scheduler;

    /**
     * 모니터링 컨트롤러를 생성한다.
     *
     * @param jobExplorer 배치 탐색기
     * @param scheduler 쿼츠 스케줄러
     */
    public BatchMonitoringController(JobExplorer jobExplorer, Scheduler scheduler) {
        this.jobExplorer = jobExplorer;
        this.scheduler = scheduler;
    }

    /**
     * 특정 잡의 실행 이력을 조회한다.
     *
     * @param jobName 잡 이름
     * @return 잡 실행 이력 목록
     */
    @GetMapping("/batch/jobs/{jobName}/executions")
    public ResponseEntity<List<BatchExecutionResponse>> getJobExecutions(@PathVariable String jobName) {
        List<JobInstance> jobInstances = jobExplorer.getJobInstances(jobName, 0, 20);
        List<BatchExecutionResponse> responses = new ArrayList<>();
        for (JobInstance jobInstance : jobInstances) {
            for (JobExecution execution : jobExplorer.getJobExecutions(jobInstance)) {
                responses.add(new BatchExecutionResponse(
                    execution.getId(),
                    jobName,
                    execution.getStatus().name(),
                    execution.getStartTime(),
                    execution.getEndTime()
                ));
            }
        }
        return ResponseEntity.ok(responses);
    }

    /**
     * 특정 잡 실행의 스텝 상세 정보를 조회한다.
     *
     * @param executionId 잡 실행 ID
     * @return 스텝 실행 정보
     */
    @GetMapping("/batch/executions/{executionId}/steps")
    public ResponseEntity<List<StepExecutionResponse>> getStepExecutions(@PathVariable Long executionId) {
        JobExecution execution = jobExplorer.getJobExecution(executionId);
        if (execution == null) {
            return ResponseEntity.notFound().build();
        }
        List<StepExecutionResponse> responses = execution.getStepExecutions().stream()
            .map(this::toStepResponse)
            .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * 쿼츠 트리거 상태를 조회한다.
     *
     * @return 트리거 상태 목록
     */
    @GetMapping("/quartz/triggers")
    public ResponseEntity<List<QuartzTriggerResponse>> getQuartzTriggers() throws Exception {
        Set<TriggerKey> triggerKeys = scheduler.getTriggerKeys(GroupMatcher.anyTriggerGroup());
        List<QuartzTriggerResponse> responses = new ArrayList<>();
        for (TriggerKey triggerKey : triggerKeys) {
            Trigger trigger = scheduler.getTrigger(triggerKey);
            String state = scheduler.getTriggerState(triggerKey).name();
            String nextFireTime = trigger.getNextFireTime() != null
                ? trigger.getNextFireTime().toInstant().toString()
                : "-";
            responses.add(new QuartzTriggerResponse(
                triggerKey.getName(),
                triggerKey.getGroup(),
                state,
                nextFireTime
            ));
        }
        return ResponseEntity.ok(responses);
    }

    /**
     * 스텝 실행 정보를 응답 모델로 변환한다.
     *
     * @param stepExecution 스텝 실행 정보
     * @return 응답 모델
     */
    private StepExecutionResponse toStepResponse(StepExecution stepExecution) {
        return new StepExecutionResponse(
            stepExecution.getId(),
            stepExecution.getStepName(),
            stepExecution.getStatus().name(),
                (int)stepExecution.getReadCount(),
                (int)stepExecution.getWriteCount(),
                (int)stepExecution.getRollbackCount(),
            stepExecution.getStartTime(),
            stepExecution.getEndTime()
        );
    }

    /**
     * 날짜를 LocalDateTime으로 변환한다.
     *
     * @param date 변환할 날짜
     * @return 변환된 LocalDateTime
     */
}
