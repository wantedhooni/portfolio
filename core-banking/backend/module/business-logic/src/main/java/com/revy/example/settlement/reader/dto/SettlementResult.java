package com.revy.example.settlement.reader.dto;

import com.revy.example.domain.billing.Settlement;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record SettlementResult(
        Long   id,
        Long   accountId,
        String type,
        LocalDate settlementDate,
        String status,
        String currency,
        BigDecimal grossAmount,
        BigDecimal feeAmount,
        BigDecimal taxAmount,
        BigDecimal netAmount,
        String referenceId,
        String note,
        String failedReason,
        Instant settledAt,
        Instant createdAt
) {
    public static SettlementResult from(Settlement s) {
        return new SettlementResult(
                s.getId(), s.getAccountId(),
                s.getType().name(), s.getSettlementDate(),
                s.getStatus().name(), s.getCurrency(),
                s.getGrossAmount(), s.getFeeAmount(),
                s.getTaxAmount(), s.getNetAmount(),
                s.getReferenceId(), s.getNote(),
                s.getFailedReason(), s.getSettledAt(),
                s.getCreatedAt()
        );
    }
}
