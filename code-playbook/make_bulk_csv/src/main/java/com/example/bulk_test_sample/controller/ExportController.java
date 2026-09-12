package com.example.bulk_test_sample.controller;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * CSV 익스포트를 요청받는 컨트롤러.
 */
@RestController
public class ExportController {

    private final Scheduler scheduler;
    private final JobDetail jobDetail;

    /**
     * 컨트롤러에 필요한 구성 요소를 주입받는다.
     *
     * @param scheduler Quartz 스케줄러
     * @param jobDetail Quartz JobDetail
     */
    public ExportController(Scheduler scheduler, JobDetail jobDetail) {
        this.scheduler = scheduler;
        this.jobDetail = jobDetail;
    }

    /**
     * CSV 익스포트를 비동기로 요청한다.
     *
     * @param isTasklet 실행 모드(tasklet/chunk)
     * @return 202 응답과 Job UUID
     */
    @PostMapping("/exports")
    public ResponseEntity<Map<String, String>> requestExport(
            @RequestParam(name = "tasklet", defaultValue = "true") boolean isTasklet
    ) {
        String jobUuid = UUID.randomUUID().toString();
        String jobName = isTasklet ? "csvExportTaskletJob" : "csvExportChunkJob";
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("jobName", jobName);
        jobDataMap.put("jobUuid", jobUuid);

        Trigger trigger = TriggerBuilder.newTrigger()
                                        .forJob(jobDetail)
                                        .usingJobData(jobDataMap)
                                        .startNow()
                                        .build();

        try {
            scheduler.scheduleJob(trigger);
        } catch (Exception ex) {
            throw new IllegalStateException("Quartz 트리거 등록 실패", ex);
        }
        return ResponseEntity.accepted().body(Map.of("jobUuid", jobUuid));
    }
}
