package com.revy.springbatchquartz.batch.listener;

import com.revy.springbatchquartz.alert.AlertNotifier;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.BatchStatus;
import org.springframework.stereotype.Component;

/**
 * 배치 잡 실행 전후를 모니터링하고 알림을 처리하는 리스너.
 */
@Component
public class BatchJobListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(BatchJobListener.class);
    private final MeterRegistry meterRegistry;
    private final AlertNotifier alertNotifier;

    /**
     * 잡 리스너를 생성한다.
     *
     * @param meterRegistry 메트릭 레지스트리
     * @param alertNotifier 알림 서비스
     */
    public BatchJobListener(MeterRegistry meterRegistry, AlertNotifier alertNotifier) {
        this.meterRegistry = meterRegistry;
        this.alertNotifier = alertNotifier;
    }

    /**
     * 잡 실행 전에 로그를 기록한다.
     *
     * @param jobExecution 잡 실행 정보
     */
    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("잡 시작: {}", jobExecution.getJobInstance().getJobName());
    }

    /**
     * 잡 실행 이후 메트릭과 알림을 처리한다.
     *
     * @param jobExecution 잡 실행 정보
     */
    @Override
    public void afterJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();
        BatchStatus status = jobExecution.getStatus();
        meterRegistry.counter("batch.job.executed", "job", jobName, "status", status.name())
            .increment();

        Duration duration = calculateDuration(jobExecution);
        Timer.builder("batch.job.duration")
            .tag("job", jobName)
            .register(meterRegistry)
            .record(duration);

        if (status == BatchStatus.FAILED) {
            String message = "잡 실패: " + jobName + " / 실행ID: " + jobExecution.getId();
            alertNotifier.notifyAll("배치 잡 실패", message);
        }
        log.info("잡 종료: {} 상태: {}", jobName, status);
    }

    /**
     * 잡 실행 시간을 계산한다.
     *
     * @param jobExecution 잡 실행 정보
     * @return 실행 시간
     */
    private Duration calculateDuration(JobExecution jobExecution) {
        LocalDateTime start = jobExecution.getStartTime() != null
            ? LocalDateTime.ofInstant(jobExecution.getStartTime().toInstant(ZoneOffset.UTC), ZoneId.systemDefault())
            : LocalDateTime.now();
        LocalDateTime end = jobExecution.getEndTime() != null
            ? LocalDateTime.ofInstant(jobExecution.getEndTime().toInstant(ZoneOffset.UTC), ZoneId.systemDefault())
            : LocalDateTime.now();
        return Duration.between(start, end);
    }
}
