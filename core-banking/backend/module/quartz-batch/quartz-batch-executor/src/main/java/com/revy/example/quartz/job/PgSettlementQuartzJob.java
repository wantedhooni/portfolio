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
 * PG 정산 및 원장 전기 Quartz Job.
 *
 * <p>Quartz Scheduler가 발화하면 Spring Batch {@code pgSettlementJob}을 실행한다.
 *
 * <h3>jobData 파라미터</h3>
 * <ul>
 *   <li>{@code targetDate} — {@code TODAY} | {@code yyyy-MM-dd}
 *       (기본값: TODAY — 당일 정산 기준)</li>
 * </ul>
 *
 * <h3>처리 흐름 (2-Step)</h3>
 * <ol>
 *   <li><b>Step 1</b> — 가맹점별 T+{settlementCycle}일 전 APPROVED 결제 집계 → PgSettlement SETTLED</li>
 *   <li><b>Step 2</b> — SETTLED PgSettlement → 원장 전기 (DR 현금 / CR 가맹점정산부채+수수료수입)</li>
 * </ol>
 *
 * <p>멱등 설계: 동일 날짜로 재실행해도 이미 처리된 가맹점·정산 건은 자동 스킵된다.
 */
@Slf4j
@Component
public class PgSettlementQuartzJob implements TypedJob {

    private final JobOperator jobOperator;


    private final Job pgSettlementJob;

    public PgSettlementQuartzJob(JobOperator jobOperator,
                                 @Qualifier("pgSettlementJob") Job pgSettlementJob) {
        this.jobOperator = jobOperator;
        this.pgSettlementJob = pgSettlementJob;
    }

    @Override
    public JobType getType() {
        return JobType.PG_SETTLEMENT;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap    = context.getMergedJobDataMap();
        String     jobKey     = context.getJobDetail().getKey().toString();
        String     instanceId = context.getFireInstanceId();

        LocalDate targetDate = resolveTargetDate(dataMap.getString("targetDate"));

        log.info("[PgSettlementBatchJob] 시작 jobKey={} instanceId={} targetDate={}",
                jobKey, instanceId, targetDate);

        JobParameters params = new JobParametersBuilder()
                .addLocalDate("targetDate", targetDate)
                .addLong("fireTime", context.getFireTime().getTime())
                .toJobParameters();

        try {
            var execution = jobOperator.start(pgSettlementJob, params);
            log.info("[PgSettlementBatchJob] 완료 jobKey={} batchStatus={} exitCode={}",
                    jobKey, execution.getStatus(), execution.getExitStatus().getExitCode());
        } catch (Exception e) {
            log.error("[PgSettlementBatchJob] 실행 실패 jobKey={} error={}", jobKey, e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }

    /** PG 정산은 당일 기준이 기본값 (T+n 계산은 Processor에서 담당). */
    private static LocalDate resolveTargetDate(String raw) {
        if (raw == null || raw.isBlank() || "TODAY".equalsIgnoreCase(raw)) {
            return LocalDate.now();
        }
        if ("YESTERDAY".equalsIgnoreCase(raw)) {
            return LocalDate.now().minusDays(1);
        }
        try {
            return LocalDate.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            log.warn("[PgSettlementBatchJob] targetDate 파싱 실패 '{}' — TODAY 로 대체", raw);
            return LocalDate.now();
        }
    }
}
