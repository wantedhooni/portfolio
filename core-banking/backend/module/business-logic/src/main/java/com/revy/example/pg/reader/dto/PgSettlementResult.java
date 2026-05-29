package com.revy.example.pg.reader.dto;

import com.revy.example.domain.pg.PgSettlement;
import com.revy.example.domain.pg.enums.PgSettlementStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record PgSettlementResult(
        Long               id,
        Long               merchantId,
        LocalDate          targetDate,
        LocalDate          settlementDate,
        int                paymentCount,
        BigDecimal         totalAmount,
        BigDecimal         commissionAmount,
        BigDecimal         netAmount,
        String             currency,
        PgSettlementStatus status,
        Instant            settledAt,
        String             failedReason,
        String             referenceId,
        Instant            createdAt
) {
    public static PgSettlementResult from(PgSettlement s) {
        return new PgSettlementResult(
                s.getId(), s.getMerchantId(), s.getTargetDate(), s.getSettlementDate(),
                s.getPaymentCount(), s.getTotalAmount(), s.getCommissionAmount(),
                s.getNetAmount(), s.getCurrency(), s.getStatus(),
                s.getSettledAt(), s.getFailedReason(), s.getReferenceId(), s.getCreatedAt()
        );
    }
}
