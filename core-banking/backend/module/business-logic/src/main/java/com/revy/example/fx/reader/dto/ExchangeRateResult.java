package com.revy.example.fx.reader.dto;

import com.revy.example.domain.fx.ExchangeRate;
import com.revy.example.domain.fx.ExchangeRateHistory;
import com.revy.example.domain.fx.enums.RateType;

import java.math.BigDecimal;
import java.time.Instant;

public record ExchangeRateResult(
        Long       id,
        String     baseCurrencyCode,
        String     quoteCurrencyCode,
        RateType   rateType,
        BigDecimal rate,
        Instant    quotedAt,
        String     source
) {
    /** 현재 환율 테이블({@link ExchangeRate})에서 변환 */
    public static ExchangeRateResult from(ExchangeRate r) {
        return new ExchangeRateResult(r.getId(), r.getBaseCurrencyCode(), r.getQuoteCurrencyCode(),
                                      r.getRateType(), r.getRate(), r.getQuotedAt(), r.getSource());
    }

    /** 이력 테이블({@link ExchangeRateHistory})에서 변환 */
    public static ExchangeRateResult fromHistory(ExchangeRateHistory h) {
        return new ExchangeRateResult(h.getId(), h.getBaseCurrencyCode(), h.getQuoteCurrencyCode(),
                                      h.getRateType(), h.getRate(), h.getQuotedAt(), h.getSource());
    }
}
