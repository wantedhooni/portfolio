package com.revy.example.fx.reader.dto;

import com.revy.example.domain.fx.FxConversion;
import com.revy.example.domain.fx.enums.FxStatus;
import com.revy.example.domain.fx.enums.RateType;

import java.math.BigDecimal;
import java.time.Instant;

public record FxConversionResult(
        Long id,
        String conversionNumber,
        Long fromAccountId,
        Long toAccountId,
        String fromCurrencyCode,
        String toCurrencyCode,
        BigDecimal fromAmount,
        BigDecimal toAmount,
        BigDecimal appliedRate,
        RateType appliedRateType,
        BigDecimal fee,
        FxStatus status,
        Long debitTxId,
        Long creditTxId,
        Instant executedAt,
        String referenceId
) {
    public static FxConversionResult from(FxConversion c) {
        return new FxConversionResult(c.getId(), c.getConversionNumber(),
            c.getFromAccountId(), c.getToAccountId(),
            c.getFromCurrencyCode(), c.getToCurrencyCode(),
            c.getFromAmount(), c.getToAmount(), c.getAppliedRate(), c.getAppliedRateType(),
            c.getFee(), c.getStatus(),
            c.getDebitTxId(), c.getCreditTxId(),
            c.getExecutedAt(), c.getReferenceId());
    }
}
