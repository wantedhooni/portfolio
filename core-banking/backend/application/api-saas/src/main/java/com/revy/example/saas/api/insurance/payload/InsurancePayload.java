package com.revy.example.saas.api.insurance.payload;

import com.revy.example.domain.insurance.enums.BeneficiaryType;
import com.revy.example.domain.insurance.enums.ClaimStatus;
import com.revy.example.domain.insurance.enums.InsuranceType;
import com.revy.example.domain.insurance.enums.PolicyStatus;
import com.revy.example.domain.insurance.enums.PremiumFrequency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class InsurancePayload {

    // ── Product ──────────────────────────────────────────────────

    @Schema(name = "SaasInsurancePayload.ProductSearchRequest")
    public record ProductSearchRequest(
            String productCode,
            String name,
            InsuranceType insuranceType,
            String currency
    ) {}

    @Schema(name = "SaasInsurancePayload.ProductResponse")
    public record ProductResponse(
            Long id,
            String productCode,
            String name,
            String description,
            InsuranceType insuranceType,
            BigDecimal basePremium,
            PremiumFrequency premiumFrequency,
            BigDecimal coverageAmount,
            Integer durationMonths,
            String currency,
            boolean isActive
    ) {}

    // ── Policy ───────────────────────────────────────────────────

    @Schema(name = "SaasInsurancePayload.EnrollRequest")
    public record EnrollRequest(
            @NotNull Long productId,
            /** 본인이 직접 피보험자인 경우 null 또는 본인 userId */
            Long insuredUserId,
            @NotNull Long billingAccountId,
            @NotNull LocalDate startDate,
            List<BeneficiaryInput> beneficiaries
    ) {}

    @Schema(name = "SaasInsurancePayload.BeneficiaryInput")
    public record BeneficiaryInput(
            @NotNull Long beneficiaryUserId,
            @NotBlank String name,
            @NotBlank String relationship,
            @NotNull @DecimalMin("0.0") BigDecimal sharePercent,
            @NotNull BeneficiaryType type
    ) {}

    @Schema(name = "SaasInsurancePayload.PolicyResponse")
    public record PolicyResponse(
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
            List<BeneficiaryResponse> beneficiaries
    ) {}

    @Schema(name = "SaasInsurancePayload.BeneficiaryResponse")
    public record BeneficiaryResponse(
            Long id,
            Long beneficiaryUserId,
            String name,
            String relationship,
            BigDecimal sharePercent,
            BeneficiaryType beneficiaryType
    ) {}

    // ── Claim ────────────────────────────────────────────────────

    @Schema(name = "SaasInsurancePayload.SubmitClaimRequest")
    public record SubmitClaimRequest(
            @NotNull Long policyId,
            @NotNull LocalDate eventDate,
            @NotBlank String claimReason,
            @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal claimAmount,
            @NotNull Long payoutAccountId
    ) {}

    @Schema(name = "SaasInsurancePayload.ClaimResponse")
    public record ClaimResponse(
            Long id,
            String claimNumber,
            Long policyId,
            Long claimantUserId,
            LocalDate eventDate,
            String claimReason,
            BigDecimal claimAmount,
            BigDecimal approvedAmount,
            Long payoutAccountId,
            ClaimStatus status,
            Instant submittedAt,
            Instant reviewedAt,
            Instant paidAt,
            String reviewNotes
    ) {}
}
