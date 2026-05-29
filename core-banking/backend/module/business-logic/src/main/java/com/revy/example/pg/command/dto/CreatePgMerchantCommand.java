package com.revy.example.pg.command.dto;

import java.math.BigDecimal;

public record CreatePgMerchantCommand(
        String     merchantCode,
        String     name,
        String     businessType,
        Long       settlementAccountId,
        BigDecimal commissionRate,
        int        settlementCycle,
        String     currency,
        String     contactEmail
) {}
