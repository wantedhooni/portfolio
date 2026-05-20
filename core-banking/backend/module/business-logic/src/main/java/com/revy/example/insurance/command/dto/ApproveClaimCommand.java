package com.revy.example.insurance.command.dto;

import java.math.BigDecimal;

public record ApproveClaimCommand(
        Long claimId,
        Long reviewerAdminId,
        BigDecimal approvedAmount,
        String reviewNotes
) {}
