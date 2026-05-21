package com.revy.example.settlement.reader.dto;

import com.revy.example.domain.billing.enums.SettlementStatus;
import com.revy.example.domain.billing.enums.SettlementType;

import java.time.LocalDate;

public record SettlementSearchCondition(
        Long accountId,
        SettlementType type,
        SettlementStatus status,
        LocalDate settlementDateFrom,
        LocalDate settlementDateTo
) {}
