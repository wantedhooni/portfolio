package com.revy.example.insurance.reader.dto;

import com.revy.example.domain.insurance.InsurancePolicy;
import com.revy.example.domain.insurance.enums.PolicyStatus;
import com.revy.example.domain.insurance.enums.PremiumFrequency;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record InsurancePolicyResult(
        Long id,
        String policyNumber,
        Long productId,
        Long userId,
        Long insuredUserId,
        Long billingAccountId,
        BigDecimal premium,
        PremiumFrequency premiumFrequency,
        BigDecimal coverageAmount,
        String currency,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate nextPaymentDate,
        PolicyStatus status,
        Instant activatedAt,
        Instant terminatedAt,
        List<BeneficiaryResult> beneficiaries
) {
    public static InsurancePolicyResult from(InsurancePolicy p) {
        return new InsurancePolicyResult(
            p.getId(), p.getPolicyNumber(), p.getProductId(),
            p.getUserId(), p.getInsuredUserId(), p.getBillingAccountId(),
            p.getPremium(), p.getPremiumFrequency(), p.getCoverageAmount(),
            p.getCurrency(), p.getStartDate(), p.getEndDate(), p.getNextPaymentDate(),
            p.getStatus(), p.getActivatedAt(), p.getTerminatedAt(),
            p.getBeneficiaries().stream().map(BeneficiaryResult::from).toList()
        );
    }
}
