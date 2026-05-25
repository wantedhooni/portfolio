package com.revy.example.quartz.enums;

import com.revy.example.quartz.exception.QuartzSchedulerException;
import org.quartz.Job;

/**
 * Quartz Job 유형 — 각 값에 실제 Job 구현 클래스 FQCN을 포함합니다.
 *
 * <p>클래스 로딩은 런타임에 {@link Class#forName}으로 수행되므로
 * api-admin은 {@code quartz-batch}에 컴파일 의존 없이 {@link JobType}만으로
 * Job을 Quartz에 등록할 수 있습니다.
 * (단, 등록 시점에 해당 클래스가 classpath에 있어야 합니다.)
 */
public enum JobType {

    SETTLEMENT           ("com.revy.example.quartz.job.SettlementJob"),
    EXCHANGE_RATE_REFRESH("com.revy.example.quartz.job.ExchangeRateRefreshJob"),
    REPORT               (null),
    NOTIFICATION         (null),
    API_CALL             (null);

    private final String jobClassName;

    JobType(String jobClassName) {
        this.jobClassName = jobClassName;
    }

    /**
     * 런타임에 Job 구현 클래스를 로딩합니다.
     *
     * @throws QuartzSchedulerException 클래스명 미등록 또는 classpath에 없는 경우
     */
    @SuppressWarnings("unchecked")
    public Class<? extends Job> toJobClass() {
        if (jobClassName == null) {
            throw new QuartzSchedulerException(
                    "JobType [" + name() + "]에 Job 클래스가 등록되어 있지 않습니다.");
        }
        try {
            return (Class<? extends Job>) Class.forName(jobClassName);
        } catch (ClassNotFoundException e) {
            throw new QuartzSchedulerException(
                    "Job 클래스를 찾을 수 없습니다 (실행 노드에서 실행하세요): " + jobClassName, e);
        }
    }
}