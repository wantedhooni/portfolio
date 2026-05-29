package com.revy.example.quartz.job;

import com.revy.example.quartz.TypedJob;
import com.revy.example.quartz.enums.JobType;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 보험료 원장 전기 Quartz Job.
 *
 * <p>Quartz Scheduler가 발화하면 Spring Batch {@code insuranceLedgerPostingJob}을 실행한다.
 *
 * <h3>jobData 파라미터</h3>
 * <ul>
 *   <li>{@code targetDate} — {@code YESTERDAY} | {@code TODAY} | {@code yyyy-MM-dd}
 *       (기본값: YESTERDAY)</li>
 * </ul>
 *
 * <h3>처리 흐름</h3>
 * <ol>
 *   <li>대상일 기준 PAID 상태의 PremiumPayment 조회 (미전기 건)</li>
 *   <li>DR 현금및현금성자산 / CR 보험료수입 분개 생성·POST</li>
 * </ol>
 */
@Slf4j
@Component

public class InsuranceLedgerPostingQuartzJob implements TypedJob {

    private final JobOperator jobOperator;


    private final Job insuranceLedgerPostingBatchJob;

    public InsuranceLedgerPostingQuartzJob(JobOperator jobOperator,
                                           @Qualifier("insuranceLedgerPostingBatchJob") Job insuranceLedgerPostingBatchJob) {
        this.jobOperator = jobOperator;
        this.insuranceLedgerPostingBatchJob = insuranceLedgerPostingBatchJob;
    }

    @Override
    public JobType getType() {
        return JobType.INSURANCE_LEDGER_POSTING;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap    = context.getMergedJobDataMap();
        String     jobKey     = context.getJobDetail().getKey().toString();
        String     instanceId = context.getFireInstanceId();

        LocalDate targetDate = resolveTargetDate(dataMap.getString("targetDate"));

        log.info("[InsuranceLedgerPostingJob] 시작 jobKey={} instanceId={} targetDate={}",
                jobKey, instanceId, targetDate);

        JobParameters params = new JobParametersBuilder()
                .addLocalDate("targetDate", targetDate)
                .addLong("fireTime", context.getFireTime().getTime())
                .toJobParameters();

        try {
            var execution = jobOperator.start(insuranceLedgerPostingBatchJob, params);
            log.info("[InsuranceLedgerPostingJob] 완료 jobKey={} batchStatus={} exitCode={}",
                    jobKey, execution.getStatus(), execution.getExitStatus().getExitCode());
        } catch (Exception e) {
            log.error("[InsuranceLedgerPostingJob] 실행 실패 jobKey={} error={}", jobKey, e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }

    private static LocalDate resolveTargetDate(String raw) {
        if (raw == null || raw.isBlank() || "YESTERDAY".equalsIgnoreCase(raw)) {
            return LocalDate.now().minusDays(1);
        }
        if ("TODAY".equalsIgnoreCase(raw)) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            log.warn("[InsuranceLedgerPostingJob] targetDate 파싱 실패 '{}' — YESTERDAY 로 대체", raw);
            return LocalDate.now().minusDays(1);
        }
    }
}
