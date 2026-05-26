package com.revy.example.quartz.service.impl;

import com.revy.example.fx.command.FxCommand;
import com.revy.example.fx.command.dto.QuoteExchangeRateCommand;
import com.revy.example.domain.fx.enums.RateType;
import com.revy.example.quartz.service.ExchangeRateRefreshResult;
import com.revy.example.quartz.service.ExchangeRateRefreshService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.revy.client.client.FrankfurterRestClient;
import com.revy.client.dto.FrankfurterRateResponse;

import java.time.Instant;
import java.util.List;

/**
 * Frankfurter API에서 환율을 조회하고 {@link FxCommand#quoteRate}로 DB에 저장하는 구현체.
 *
 * <p><b>처리 흐름:</b>
 * <ol>
 *   <li>Frankfurter {@code /v2/rates} 호출 → base/quote 별 mid-rate 수신</li>
 *   <li>각 통화쌍에 대해 {@code FxCommand.quoteRate()} 호출 — append-only 저장</li>
 *   <li>개별 실패 시 로그 기록 후 나머지 쌍 계속 처리 (부분 성공 허용)</li>
 * </ol>
 *
 * <p>Frankfurter는 mid-market 환율을 제공하므로 {@link RateType#MID}로 저장합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRateRefreshServiceImpl implements ExchangeRateRefreshService {

    private static final String SOURCE = "frankfurter";

    private final FrankfurterRestClient frankfurterRestClient;
    private final FxCommand             fxCommand;

    @Override
    public ExchangeRateRefreshResult refresh(String baseCurrency, List<String> quoteCurrencies) {
        Instant refreshedAt = Instant.now();

        log.info("[ExchangeRateRefresh] API 호출 base={} quotes={}", baseCurrency, quoteCurrencies);
        List<FrankfurterRateResponse> rates =
                frankfurterRestClient.getLatestRates(baseCurrency, quoteCurrencies);

        int successCount = 0;
        int failedCount  = 0;

        for (FrankfurterRateResponse rate : rates) {
            try {
                fxCommand.quoteRate(new QuoteExchangeRateCommand(
                        rate.base(),
                        rate.quote(),
                        RateType.MID,
                        rate.rate(),
                        refreshedAt,
                        SOURCE
                ));
                log.debug("[ExchangeRateRefresh] 저장 완료 pair={}/{} rate={}", rate.base(), rate.quote(), rate.rate());
                successCount++;
            } catch (Exception e) {
                log.error("[ExchangeRateRefresh] 저장 실패 pair={}/{} error={}",
                        rate.base(), rate.quote(), e.getMessage(), e);
                failedCount++;
            }
        }

        log.info("[ExchangeRateRefresh] 완료 base={} total={} success={} failed={}",
                baseCurrency, rates.size(), successCount, failedCount);

        return new ExchangeRateRefreshResult(baseCurrency, rates.size(), successCount, failedCount, refreshedAt);
    }
}
