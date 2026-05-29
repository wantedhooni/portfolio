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
 * 증권 정산 및 원장 전기 Quartz Job.
 *
 * <p>Quartz Scheduler가 발화하면 Spring Batch {@code securitiesSettlementJob}을 실행한다.
 *
 * <h3>jobData 파라미터</h3>
 * <ul>
 *   <li>{@code targetDate} — {@code YESTERDAY} | {@code TODAY} | {@code yyyy-MM-dd}
 *       (기본값: YESTERDAY)</li>
 * </ul>
 *
 * <h3>처리 흐름 (2-Step)</h3>
 * <ol>
 *   <li><b>Step 1</b> — 체결 완료(COMPLETED) AccountTx → Settlement 생성 → SETTLED</li>
 *   <li><b>Step 2</b> — SETTLED Settlement → 원장 분개 생성·POST</li>
 * </ol>
 *
 * <p>각 Step은 멱등적으로 동작하므로 동일 대상일 재실행 시 이미 처리된 건은 자동 스킵된다.
 */
@Slf4j
@Component
public class SecuritiesSettlementQuartzJob implements TypedJob {

    private final JobOperator jobOperator;


    private final Job securitiesSettlementJob;

    public SecuritiesSettlementQuartzJob(JobOperator jobOperator,
                                         @Qualifier("securitiesSettlementJob") Job securitiesSettlementJob) {
        this.jobOperator = jobOperator;
        this.securitiesSettlementJob = securitiesSettlementJob;
    }

    @Override
    public JobType getType() {
        return JobType.SECURITIES_SETTLEMENT;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap    = context.getMergedJobDataMap();
        String     jobKey     = context.getJobDetail().getKey().toString();
        String     instanceId = context.getFireInstanceId();

        LocalDate targetDate = resolveTargetDate(dataMap.getString("targetDate"));

        log.info("[SecuritiesSettlementBatchJob] 시작 jobKey={} instanceId={} targetDate={}",
                jobKey, instanceId, targetDate);

        JobParameters params = new JobParametersBuilder()
                .addLocalDate("targetDate", targetDate)
                .addLong("fireTime", context.getFireTime().getTime())
                .toJobParameters();

        try {
            var execution = jobOperator.start(securitiesSettlementJob, params);
            log.info("[SecuritiesSettlementBatchJob] 완료 jobKey={} batchStatus={} exitCode={}",
                    jobKey, execution.getStatus(), execution.getExitStatus().getExitCode());
        } catch (Exception e) {
            log.error("[SecuritiesSettlementBatchJob] 실행 실패 jobKey={} error={}", jobKey, e.getMessage(), e);
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
            log.warn("[SecuritiesSettlementBatchJob] targetDate 파싱 실패 '{}' — YESTERDAY 로 대체", raw);
            return LocalDate.now().minusDays(1);
        }
    }
}
