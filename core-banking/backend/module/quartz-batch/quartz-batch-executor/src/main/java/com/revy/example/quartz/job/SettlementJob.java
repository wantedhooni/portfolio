package com.revy.example.quartz.job;

import com.revy.example.quartz.TypedJob;
import com.revy.example.quartz.enums.JobType;
import com.revy.example.quartz.service.InsurancePremiumSettlementResult;
import com.revy.example.quartz.service.InsurancePremiumSettlementService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 보험료 정산 Quartz Job.
 *
 * <p><b>jobData 파라미터:</b>
 * <ul>
 *   <li>{@code targetDate} — {@code YESTERDAY}, {@code TODAY}, 또는 {@code yyyy-MM-dd} (기본값: YESTERDAY)</li>
 * </ul>
 *
 * <p>Spring DI는 {@code QuartzAutoConfiguration} 이 구성한
 * {@code SpringBeanJobFactory} 가 처리한다.
 */
@Slf4j
@Component
public class SettlementJob implements TypedJob {

    @Autowired
    private InsurancePremiumSettlementService settlementService;

    @Override
    public JobType getType() {
        return JobType.SETTLEMENT;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap     = context.getMergedJobDataMap();
        String fireInstanceId  = context.getFireInstanceId();
        String jobKey          = context.getJobDetail().getKey().toString();

        LocalDate targetDate = resolveTargetDate(dataMap.getString("targetDate"));

        log.info("[SettlementJob] 시작 jobKey={} fireInstanceId={} targetDate={}",
                jobKey, fireInstanceId, targetDate);

        try {
            InsurancePremiumSettlementResult result = settlementService.execute(targetDate);
            log.info("[SettlementJob] 완료 jobKey={} result={}", jobKey, result);
        } catch (Exception e) {
            log.error("[SettlementJob] 실행 실패 jobKey={} error={}", jobKey, e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }

    // ── 내부 ─────────────────────────────────────────────────────────────────

    /**
     * targetDate 문자열 파싱.
     * <ul>
     *   <li>{@code null} / {@code ""} / {@code "YESTERDAY"} → 어제</li>
     *   <li>{@code "TODAY"} → 오늘</li>
     *   <li>{@code "yyyy-MM-dd"} → 해당 날짜</li>
     * </ul>
     */
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
            log.warn("[SettlementJob] targetDate 파싱 실패 '{}' — YESTERDAY 로 대체", raw);
            return LocalDate.now().minusDays(1);
        }
    }
}
