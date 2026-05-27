package com.revy.example.fx.reader.dto;

import com.revy.example.domain.fx.FxCorridor;
import com.revy.example.domain.fx.enums.CorridorStatus;

import java.math.BigDecimal;

public record FxCorridorResult(
        Long           id,
        String         baseCurrencyCode,
        String         quoteCurrencyCode,
        BigDecimal     minAmount,
        BigDecimal     maxAmount,
        BigDecimal     dailyLimit,
        BigDecimal     spreadRate,
        CorridorStatus status
) {
    public static FxCorridorResult from(FxCorridor c) {
        return new FxCorridorResult(
            c.getId(),
            c.getBaseCurrencyCode(),
            c.getQuoteCurrencyCode(),
            c.getMinAmount(),
            c.getMaxAmount(),
            c.getDailyLimit(),
            c.getSpreadRate(),
            c.getStatus()
        );
    }
}
