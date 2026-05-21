package com.revy.example.settlement.command.dto;

import com.revy.example.domain.billing.enums.SettlementType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateSettlementCommand(
        Long accountId,
        SettlementType type,
        LocalDate settlementDate,
        String currency,
        BigDecimal grossAmount,
        BigDecimal feeAmount,
        BigDecimal taxAmount,
        BigDecimal netAmount,
        String referenceId,
        String note
) {}
