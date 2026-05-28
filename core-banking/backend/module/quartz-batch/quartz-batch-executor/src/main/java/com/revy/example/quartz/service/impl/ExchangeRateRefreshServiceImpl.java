package com.revy.example.quartz.service.impl;

import com.revy.client.client.FrankfurterRestClient;
import com.revy.client.dto.FrankfurterRateResponse;
import com.revy.example.domain.fx.enums.RateType;
import com.revy.example.fx.command.FxCommand;
import com.revy.example.fx.command.dto.QuoteExchangeRateCommand;
import com.revy.example.quartz.service.ExchangeRateRefreshResult;
import com.revy.example.quartz.service.ExchangeRateRefreshService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Frankfurter API에서 환율을 조회하고 {@link FxCommand#quoteRate}로 DB에 저장하는 구현체.
 *
 * <ul>
 *   <li>{@link #refresh} — 지정한 quotes 목록만 조회 (FxCorridor 기반)</li>
 *   <li>{@link #refreshAll} — quotes 파라미터 없이 Frankfurter 제공 전체 통화 조회</li>
 * </ul>
 *
 * <p>Frankfurter는 mid-market 환율을 제공하므로 {@link RateType#MID}로 저장합니다.</p>
 * <p>개별 저장 실패는 로그만 남기고 나머지 쌍은 계속 처리(부분 성공 허용)합니다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRateRefreshServiceImpl implements ExchangeRateRefreshService {

    private static final String SOURCE = "frankfurter";

    private final FrankfurterRestClient frankfurterRestClient;
    private final FxCommand             fxCommand;

    // ── public ──────────────────────────────────────────────────

    @Override
    public ExchangeRateRefreshResult refreshAll(String baseCurrency) {
        Instant refreshedAt = Instant.now();
        log.info("[ExchangeRateRefresh] 전체 최신 환율 조회 base={}", baseCurrency);

        List<FrankfurterRateResponse> rates = frankfurterRestClient.getLatestRates(baseCurrency);
        return save(baseCurrency, rates, refreshedAt);
    }

    @Override
    public ExchangeRateRefreshResult refresh(String baseCurrency, List<String> quoteCurrencies) {
        Instant refreshedAt = Instant.now();
        log.info("[ExchangeRateRefresh] 지정 통화 조회 base={} quotes={}", baseCurrency, quoteCurrencies);

        List<FrankfurterRateResponse> rates = frankfurterRestClient.getRates(baseCurrency, quoteCurrencies);
        return save(baseCurrency, rates, refreshedAt);
    }

    // ── private ──────────────────────────────────────────────────

    /** 조회된 환율 목록을 DB에 저장하고 결과를 반환합니다. */
    private ExchangeRateRefreshResult save(String baseCurrency,
                                           List<FrankfurterRateResponse> rates,
                                           Instant refreshedAt) {
        int successCount = 0;
        int failedCount  = 0;

        for (FrankfurterRateResponse rate : rates) {
            try {
                fxCommand.quoteRate(new QuoteExchangeRateCommand(
                    rate.base(), rate.quote(),
                    RateType.MID, rate.rate(),
                    refreshedAt, SOURCE
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
