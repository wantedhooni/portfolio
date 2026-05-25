package com.revy.example.quartz.job;

import com.revy.example.quartz.TypedJob;
import com.revy.example.quartz.enums.JobType;
import com.revy.example.quartz.service.ExchangeRateRefreshResult;
import com.revy.example.quartz.service.ExchangeRateRefreshService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Frankfurter 환율 자동 갱신 Quartz Job.
 *
 * <p><b>jobData 파라미터:</b>
 * <ul>
 *   <li>{@code baseCurrency}    — 기준 통화 코드 (기본값: {@code USD})</li>
 *   <li>{@code quoteCurrencies} — 쉼표 구분 상대 통화 목록 (기본값: {@code KRW,EUR,JPY,GBP,CNY})</li>
 * </ul>
 *
 * <p>Spring DI는 {@code SpringBeanJobFactory}가 처리하므로 {@code @Autowired}로 주입합니다.
 */
@Slf4j
@Component
public class ExchangeRateRefreshJob implements TypedJob {

    private static final String       DEFAULT_BASE   = "USD";
    private static final List<String> DEFAULT_QUOTES = List.of("KRW", "EUR", "JPY", "GBP", "CNY");

    @Autowired
    private ExchangeRateRefreshService refreshService;

    @Override
    public JobType getType() {
        return JobType.EXCHANGE_RATE_REFRESH;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap       = context.getMergedJobDataMap();
        String     jobKey        = context.getJobDetail().getKey().toString();
        String     fireInstanceId = context.getFireInstanceId();

        String baseCurrency = Optional.ofNullable(dataMap.getString("baseCurrency"))
                .filter(s -> !s.isBlank())
                .orElse(DEFAULT_BASE);

        List<String> quoteCurrencies = Optional.ofNullable(dataMap.getString("quoteCurrencies"))
                .filter(s -> !s.isBlank())
                .map(s -> Arrays.asList(s.split(",")))
                .orElse(DEFAULT_QUOTES);

        log.info("[ExchangeRateRefreshJob] 시작 jobKey={} fireInstanceId={} base={} quotes={}",
                jobKey, fireInstanceId, baseCurrency, quoteCurrencies);

        try {
            ExchangeRateRefreshResult result = refreshService.refresh(baseCurrency, quoteCurrencies);
            log.info("[ExchangeRateRefreshJob] 완료 jobKey={} result={}", jobKey, result);
        } catch (Exception e) {
            log.error("[ExchangeRateRefreshJob] 실행 실패 jobKey={} error={}", jobKey, e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }
}
