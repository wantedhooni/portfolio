package com.revy.example.insurance.reader.dto;

import com.revy.example.domain.insurance.InsuranceClaim;
import com.revy.example.domain.insurance.enums.ClaimStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record InsuranceClaimResult(
        Long id,
        String claimNumber,
        Long policyId,
        Long claimantUserId,
        LocalDate eventDate,
        String claimReason,
        BigDecimal claimAmount,
        BigDecimal approvedAmount,
        Long payoutAccountId,
        Long accountTxId,
        ClaimStatus status,
        Instant submittedAt,
        Instant reviewedAt,
        Instant paidAt,
        Long reviewerAdminId,
        String reviewNotes
) {
    public static InsuranceClaimResult from(InsuranceClaim c) {
        return new InsuranceClaimResult(
            c.getId(), c.getClaimNumber(), c.getPolicyId(), c.getClaimantUserId(),
            c.getEventDate(), c.getClaimReason(), c.getClaimAmount(), c.getApprovedAmount(),
            c.getPayoutAccountId(), c.getAccountTxId(), c.getStatus(),
            c.getSubmittedAt(), c.getReviewedAt(), c.getPaidAt(),
            c.getReviewerAdminId(), c.getReviewNotes()
        );
    }
}
