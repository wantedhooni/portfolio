package com.revy.example.pg.reader.dto;

import com.revy.example.domain.pg.PgMerchant;

import java.math.BigDecimal;
import java.time.Instant;

public record PgMerchantResult(
        Long       id,
        String     merchantCode,
        String     name,
        String     businessType,
        Long       settlementAccountId,
        BigDecimal commissionRate,
        int        settlementCycle,
        String     currency,
        boolean    isActive,
        String     contactEmail,
        Instant    createdAt
) {
    public static PgMerchantResult from(PgMerchant m) {
        return new PgMerchantResult(
                m.getId(), m.getMerchantCode(), m.getName(), m.getBusinessType(),
                m.getSettlementAccountId(), m.getCommissionRate(), m.getSettlementCycle(),
                m.getCurrency(), m.isActive(), m.getContactEmail(), m.getCreatedAt()
        );
    }
}
