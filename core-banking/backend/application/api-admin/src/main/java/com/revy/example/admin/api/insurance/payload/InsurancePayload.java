package com.revy.example.admin.api.insurance.payload;

import com.revy.example.domain.insurance.enums.BeneficiaryType;
import com.revy.example.domain.insurance.enums.ClaimStatus;
import com.revy.example.domain.insurance.enums.InsuranceType;
import com.revy.example.domain.insurance.enums.PolicyStatus;
import com.revy.example.domain.insurance.enums.PremiumFrequency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class InsurancePayload {

    // ── Product ──────────────────────────────────────────────────

    @Schema(name = "InsurancePayload.CreateProductRequest")
    public record CreateProductRequest(
            @NotBlank String productCode,
            @NotBlank String name,
            String description,
            @NotNull InsuranceType insuranceType,
            @NotNull @DecimalMin("0.0") BigDecimal basePremium,
            @NotNull PremiumFrequency premiumFrequency,
            @NotNull @DecimalMin("0.0") BigDecimal coverageAmount,
            @NotNull @Min(1) Integer durationMonths,
            @NotBlank String currency
    ) {}

    @Schema(name = "InsurancePayload.UpdateProductPricingRequest")
    public record UpdateProductPricingRequest(
            @NotNull @DecimalMin("0.0") BigDecimal basePremium,
            @NotNull @DecimalMin("0.0") BigDecimal coverageAmount
    ) {}

    @Schema(name = "InsurancePayload.ProductSearchRequest")
    public record ProductSearchRequest(
            String productCode,
            String name,
            InsuranceType insuranceType,
            String currency,
            Boolean isActive
    ) {}

    @Schema(name = "InsurancePayload.ProductResponse")
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

    @Schema(name = "InsurancePayload.EnrollPolicyRequest")
    public record EnrollPolicyRequest(
            @NotNull Long productId,
            @NotNull Long userId,
            @NotNull Long insuredUserId,
            @NotNull Long billingAccountId,
            @NotNull LocalDate startDate,
            List<BeneficiaryInput> beneficiaries
    ) {}

    @Schema(name = "InsurancePayload.BeneficiaryInput")
    public record BeneficiaryInput(
            @NotNull Long beneficiaryUserId,
            @NotBlank String name,
            @NotBlank String relationship,
            @NotNull @DecimalMin("0.0") BigDecimal sharePercent,
            @NotNull BeneficiaryType type
    ) {}

    @Schema(name = "InsurancePayload.PolicySearchRequest")
    public record PolicySearchRequest(
            String policyNumber,
            Long userId,
            Long insuredUserId,
            Long productId,
            PolicyStatus status
    ) {}

    @Schema(name = "InsurancePayload.PolicyResponse")
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

    @Schema(name = "InsurancePayload.BeneficiaryResponse")
    public record BeneficiaryResponse(
            Long id,
            Long beneficiaryUserId,
            String name,
            String relationship,
            BigDecimal sharePercent,
            BeneficiaryType beneficiaryType
    ) {}

    // ── Premium Payment ──────────────────────────────────────────

    @Schema(name = "InsurancePayload.PayPremiumRequest")
    public record PayPremiumRequest(
            @NotNull Long paymentId,
            @NotBlank String referenceId
    ) {}

    @Schema(name = "InsurancePayload.PremiumSearchRequest")
    public record PremiumSearchRequest(
            Long      policyId,
            String    status,
            LocalDate dueDateFrom,
            LocalDate dueDateTo
    ) {}

    @Schema(name = "InsurancePayload.SchedulePremiumRequest")
    public record SchedulePremiumRequest(
            @NotNull                      Long       policyId,
            @NotNull @DecimalMin("0.01") BigDecimal amount,
            @NotBlank                     String     currency,
            @NotNull                      LocalDate  dueDate,
            @NotNull                      Long       billingAccountId,
            @NotBlank                     String     referenceId
    ) {}

    @Schema(name = "InsurancePayload.PremiumPaymentResponse")
    public record PremiumPaymentResponse(
            Long       id,
            Long       policyId,
            BigDecimal amount,
            String     currency,
            LocalDate  dueDate,
            String     status,
            Instant    paidAt,
            Long       billingAccountId,
            Long       accountTxId,
            String     referenceId,
            String     failureReason
    ) {}

    // ── Claim ────────────────────────────────────────────────────

    @Schema(name = "InsurancePayload.SubmitClaimRequest")
    public record SubmitClaimRequest(
            @NotNull Long policyId,
            @NotNull Long claimantUserId,
            @NotNull LocalDate eventDate,
            @NotBlank String claimReason,
            @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal claimAmount,
            @NotNull Long payoutAccountId
    ) {}

    @Schema(name = "InsurancePayload.ApproveClaimRequest")
    public record ApproveClaimRequest(
            @NotNull Long reviewerAdminId,
            @NotNull @DecimalMin("0.0") BigDecimal approvedAmount,
            String reviewNotes
    ) {}

    @Schema(name = "InsurancePayload.RejectClaimRequest")
    public record RejectClaimRequest(
            @NotNull Long reviewerAdminId,
            String reviewNotes
    ) {}

    @Schema(name = "InsurancePayload.PayClaimRequest")
    public record PayClaimRequest(
            @NotBlank String referenceId
    ) {}

    @Schema(name = "InsurancePayload.ClaimSearchRequest")
    public record ClaimSearchRequest(
            String claimNumber,
            Long policyId,
            Long claimantUserId,
            ClaimStatus status
    ) {}

    @Schema(name = "InsurancePayload.ClaimResponse")
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
            Long accountTxId,
            ClaimStatus status,
            Instant submittedAt,
            Instant reviewedAt,
            Instant paidAt,
            Long reviewerAdminId,
            String reviewNotes
    ) {}
}
