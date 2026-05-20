package com.revy.example.fx.command.dto;

import com.revy.example.domain.fx.enums.RateType;

import java.math.BigDecimal;
import java.time.Instant;

public record QuoteExchangeRateCommand(
        String baseCurrencyCode,
        String quoteCurrencyCode,
        RateType rateType,
        BigDecimal rate,
        Instant quotedAt,
        String source
) {}
