package com.revy.example.fx.reader.dto;

import com.revy.example.domain.fx.ExchangeRate;
import com.revy.example.domain.fx.enums.RateType;

import java.math.BigDecimal;
import java.time.Instant;

public record ExchangeRateResult(
        Long id,
        String baseCurrencyCode,
        String quoteCurrencyCode,
        RateType rateType,
        BigDecimal rate,
        Instant quotedAt,
        String source
) {
    public static ExchangeRateResult from(ExchangeRate r) {
        return new ExchangeRateResult(r.getId(), r.getBaseCurrencyCode(), r.getQuoteCurrencyCode(),
                                      r.getRateType(), r.getRate(), r.getQuotedAt(), r.getSource());
    }
}
