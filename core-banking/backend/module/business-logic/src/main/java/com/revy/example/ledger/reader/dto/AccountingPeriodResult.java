package com.revy.example.ledger.reader.dto;

import com.revy.example.domain.ledger.AccountingPeriod;
import com.revy.example.domain.ledger.enums.PeriodStatus;

import java.time.Instant;
import java.time.LocalDate;

public record AccountingPeriodResult(
        Long id,
        Integer fiscalYear,
        Integer fiscalPeriod,
        LocalDate startDate,
        LocalDate endDate,
        PeriodStatus status,
        Instant closedAt,
        Long closedByAdminId
) {
    public static AccountingPeriodResult from(AccountingPeriod p) {
        return new AccountingPeriodResult(
            p.getId(), p.getFiscalYear(), p.getFiscalPeriod(),
            p.getStartDate(), p.getEndDate(), p.getStatus(),
            p.getClosedAt(), p.getClosedByAdminId()
        );
    }
}
