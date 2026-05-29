package com.revy.example.quartz.job;

import com.revy.example.fx.reader.FxReader;
import com.revy.example.fx.reader.dto.FxCorridorResult;
import com.revy.example.quartz.TypedJob;
import com.revy.example.quartz.enums.JobType;
import com.revy.example.quartz.service.ExchangeRateRefreshResult;
import com.revy.example.quartz.service.ExchangeRateRefreshService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
@RequiredArgsConstructor
public class ExchangeRateRefreshQuartzJob implements TypedJob {

    private final ExchangeRateRefreshService refreshService;
    private final FxReader fxReader;

    @Override
    public JobType getType() {
        return JobType.EXCHANGE_RATE_REFRESH;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap       = context.getMergedJobDataMap();
        String     jobKey        = context.getJobDetail().getKey().toString();
        String     fireInstanceId = context.getFireInstanceId();

        List<FxCorridorResult> allActiveCorridors = fxReader.findAllActiveCorridors();

        Map<String, List<String>> mapCorriListMap = allActiveCorridors.stream().collect(Collectors.groupingBy(
            FxCorridorResult::baseCurrencyCode,
            Collectors.mapping(FxCorridorResult::quoteCurrencyCode, Collectors.toList())
        ));



        try {
            mapCorriListMap.forEach((baseCurrency, quoteCurrencies) -> {
                log.info("[ExchangeRateRefreshJob] 시작 jobKey={} fireInstanceId={} base={} quotes={}",
                         jobKey, fireInstanceId, baseCurrency, quoteCurrencies);
                ExchangeRateRefreshResult result = refreshService.refresh(baseCurrency, quoteCurrencies);
                log.info("[ExchangeRateRefreshJob] 완료 jobKey={} result={}", jobKey, result);
            });
        } catch (Exception e) {
            log.error("[ExchangeRateRefreshJob] 실행 실패 jobKey={} error={}", jobKey, e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }
}
