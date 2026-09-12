package com.revy.springbatchquartz.batch.listener;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

/**
 * 배치 스텝 실행을 모니터링하는 리스너.
 */
@Component
public class BatchStepListener implements StepExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(BatchStepListener.class);
    private final MeterRegistry meterRegistry;

    /**
     * 스텝 리스너를 생성한다.
     *
     * @param meterRegistry 메트릭 레지스트리
     */
    public BatchStepListener(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * 스텝 시작 전에 로그를 기록한다.
     *
     * @param stepExecution 스텝 실행 정보
     */
    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("스텝 시작: {}", stepExecution.getStepName());
    }

    /**
     * 스텝 종료 후 메트릭을 기록하고 상태를 반환한다.
     *
     * @param stepExecution 스텝 실행 정보
     * @return 종료 상태
     */
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        String stepName = stepExecution.getStepName();
        meterRegistry.counter("batch.step.executed", "step", stepName,
            "status", stepExecution.getStatus().name())
            .increment();

        Timer.builder("batch.step.duration")
            .tag("step", stepName)
            .register(meterRegistry)
            .record(calculateDuration(stepExecution));

        log.info("스텝 종료: {} 상태: {}", stepName, stepExecution.getStatus());
        return stepExecution.getExitStatus();
    }

    /**
     * 스텝 실행 시간을 계산한다.
     *
     * @param stepExecution 스텝 실행 정보
     * @return 실행 시간
     */
    private Duration calculateDuration(StepExecution stepExecution) {
        LocalDateTime start = stepExecution.getStartTime() != null
            ? LocalDateTime.ofInstant(stepExecution.getStartTime().toInstant(ZoneOffset.UTC), ZoneId.systemDefault())
            : LocalDateTime.now();
        LocalDateTime end = stepExecution.getEndTime() != null
            ? LocalDateTime.ofInstant(stepExecution.getEndTime().toInstant(ZoneOffset.UTC), ZoneId.systemDefault())
            : LocalDateTime.now();
        return Duration.between(start, end);
    }
}
