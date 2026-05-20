package com.revy.example.insurance.command.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SubmitClaimCommand(
        Long policyId,
        Long claimantUserId,
        LocalDate eventDate,
        String claimReason,
        BigDecimal claimAmount,
        Long payoutAccountId
) {}
