package com.revy.example.admin.api.insurance.usecase.impl;

import com.revy.example.admin.api.insurance.payload.InsurancePayload;
import com.revy.example.admin.api.insurance.usecase.InsuranceUseCase;
import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.insurance.command.InsuranceCommand;
import com.revy.example.insurance.command.dto.ApproveClaimCommand;
import com.revy.example.insurance.command.dto.CreateInsuranceProductCommand;
import com.revy.example.insurance.command.dto.EnrollPolicyCommand;
import com.revy.example.insurance.command.dto.PayClaimCommand;
import com.revy.example.insurance.command.dto.PayPremiumCommand;
import com.revy.example.insurance.command.dto.SubmitClaimCommand;
import com.revy.example.insurance.reader.InsuranceReader;
import com.revy.example.insurance.reader.dto.BeneficiaryResult;
import com.revy.example.insurance.reader.dto.InsuranceClaimResult;
import com.revy.example.insurance.reader.dto.InsuranceClaimSearchCondition;
import com.revy.example.insurance.reader.dto.InsurancePolicyResult;
import com.revy.example.insurance.reader.dto.InsurancePolicySearchCondition;
import com.revy.example.insurance.reader.dto.InsuranceProductResult;
import com.revy.example.insurance.reader.dto.InsuranceProductSearchCondition;
import com.revy.example.insurance.reader.dto.PremiumPaymentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InsuranceUseCaseImpl implements InsuranceUseCase {

    private final InsuranceReader  insuranceReader;
    private final InsuranceCommand insuranceCommand;

    // ── Product ──────────────────────────────────────────────────

    @Override
    public InsurancePayload.ProductResponse createProduct(InsurancePayload.CreateProductRequest request) {
        Long id = insuranceCommand.createProduct(new CreateInsuranceProductCommand(
            request.productCode(), request.name(), request.description(),
            request.insuranceType(), request.basePremium(), request.premiumFrequency(),
            request.coverageAmount(), request.durationMonths(), request.currency()
        ));
        return getProduct(id);
    }

    @Override
    public InsurancePayload.ProductResponse getProduct(Long id) {
        return insuranceReader.findProductById(id)
            .map(this::toProductResponse)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "InsuranceProduct id=" + id));
    }

    @Override
    public ApiPageResponse<InsurancePayload.ProductResponse> searchProducts(
            Pageable pageable, InsurancePayload.ProductSearchRequest request) {
        InsuranceProductSearchCondition condition = InsuranceProductSearchCondition.builder()
            .productCode(request.productCode())
            .name(request.name())
            .insuranceType(request.insuranceType())
            .currency(request.currency())
            .isActive(request.isActive())
            .build();
        Page<InsuranceProductResult> page = insuranceReader.searchProducts(pageable, condition);
        return ApiPageResponse.of(
            page.getContent().stream().map(this::toProductResponse).toList(),
            page.getTotalElements(), page.getNumber(), page.getSize()
        );
    }

    @Override
    public void updateProductPricing(Long id, InsurancePayload.UpdateProductPricingRequest request) {
        insuranceCommand.updateProductPricing(id, request.basePremium(), request.coverageAmount());
    }

    @Override
    public void discontinueProduct(Long id) {
        insuranceCommand.discontinueProduct(id);
    }

    // ── Policy ───────────────────────────────────────────────────

    @Override
    public InsurancePayload.PolicyResponse enrollPolicy(InsurancePayload.EnrollPolicyRequest request) {
        List<EnrollPolicyCommand.BeneficiaryInput> beneficiaries = null;
        if (request.beneficiaries() != null) {
            beneficiaries = request.beneficiaries().stream()
                .map(b -> new EnrollPolicyCommand.BeneficiaryInput(
                    b.beneficiaryUserId(), b.name(), b.relationship(), b.sharePercent(), b.type()
                )).toList();
        }
        Long id = insuranceCommand.enrollPolicy(new EnrollPolicyCommand(
            request.productId(), request.userId(), request.insuredUserId(),
            request.billingAccountId(), request.startDate(), beneficiaries
        ));
        return getPolicy(id);
    }

    @Override
    public InsurancePayload.PolicyResponse getPolicy(Long id) {
        return insuranceReader.findPolicyById(id)
            .map(this::toPolicyResponse)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "InsurancePolicy id=" + id));
    }

    @Override
    public ApiPageResponse<InsurancePayload.PolicyResponse> searchPolicies(
            Pageable pageable, InsurancePayload.PolicySearchRequest request) {
        InsurancePolicySearchCondition condition = InsurancePolicySearchCondition.builder()
            .policyNumber(request.policyNumber())
            .userId(request.userId())
            .insuredUserId(request.insuredUserId())
            .productId(request.productId())
            .status(request.status())
            .build();
        Page<InsurancePolicyResult> page = insuranceReader.searchPolicies(pageable, condition);
        return ApiPageResponse.of(
            page.getContent().stream().map(this::toPolicyResponse).toList(),
            page.getTotalElements(), page.getNumber(), page.getSize()
        );
    }

    @Override
    public void activatePolicy(Long id)    { insuranceCommand.activatePolicy(id);    }

    @Override
    public void suspendPolicy(Long id)     { insuranceCommand.suspendPolicy(id);     }

    @Override
    public void reactivatePolicy(Long id)  { insuranceCommand.reactivatePolicy(id);  }

    @Override
    public void terminatePolicy(Long id)   { insuranceCommand.terminatePolicy(id);   }

    @Override
    public void cancelPolicy(Long id)      { insuranceCommand.cancelPolicy(id);      }

    // ── Premium Payment ──────────────────────────────────────────

    @Override
    public InsurancePayload.PremiumPaymentResponse getPremiumPayment(Long paymentId) {
        return insuranceReader.findPaymentById(paymentId)
                .map(this::toPremiumResponse)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND,
                        "PremiumPayment id=" + paymentId));
    }

    @Override
    public ApiPageResponse<InsurancePayload.PremiumPaymentResponse> searchPremiumPayments(
            Pageable pageable, InsurancePayload.PremiumSearchRequest req) {
        Page<PremiumPaymentResult> page = insuranceReader.searchPayments(
                pageable, req.policyId(), req.status(), req.dueDateFrom(), req.dueDateTo());
        return ApiPageResponse.of(
                page.getContent().stream().map(this::toPremiumResponse).toList(),
                page.getTotalElements(), page.getNumber(), page.getSize());
    }

    @Override
    public List<InsurancePayload.PremiumPaymentResponse> getPaymentsByPolicy(Long policyId) {
        return insuranceReader.findAllPaymentsByPolicyId(policyId).stream()
                .map(this::toPremiumResponse).toList();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public InsurancePayload.PremiumPaymentResponse schedulePremiumPayment(
            InsurancePayload.SchedulePremiumRequest req) {
        Long id = insuranceCommand.schedulePremiumPayment(
                req.policyId(), req.amount(), req.currency(),
                req.dueDate(), req.billingAccountId(), req.referenceId());
        return getPremiumPayment(id);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void payPremium(Long policyId, InsurancePayload.PayPremiumRequest request) {
        insuranceCommand.payPremium(new PayPremiumCommand(request.paymentId(), request.referenceId()));
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void payPremiumById(Long paymentId, String referenceId) {
        insuranceCommand.payPremium(new PayPremiumCommand(paymentId, referenceId));
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void markPremiumOverdue(Long paymentId) {
        insuranceCommand.markPremiumOverdue(paymentId);
    }

    private InsurancePayload.PremiumPaymentResponse toPremiumResponse(PremiumPaymentResult r) {
        return new InsurancePayload.PremiumPaymentResponse(
                r.id(), r.policyId(), r.amount(), r.currency(), r.dueDate(),
                r.status().name(), r.paidAt(), r.billingAccountId(),
                r.accountTxId(), r.referenceId(), r.failureReason());
    }

    // ── Claim ────────────────────────────────────────────────────

    @Override
    public InsurancePayload.ClaimResponse submitClaim(InsurancePayload.SubmitClaimRequest request) {
        Long id = insuranceCommand.submitClaim(new SubmitClaimCommand(
            request.policyId(), request.claimantUserId(), request.eventDate(),
            request.claimReason(), request.claimAmount(), request.payoutAccountId()
        ));
        return getClaim(id);
    }

    @Override
    public InsurancePayload.ClaimResponse getClaim(Long id) {
        return insuranceReader.findClaimById(id)
            .map(this::toClaimResponse)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "InsuranceClaim id=" + id));
    }

    @Override
    public ApiPageResponse<InsurancePayload.ClaimResponse> searchClaims(
            Pageable pageable, InsurancePayload.ClaimSearchRequest request) {
        InsuranceClaimSearchCondition condition = InsuranceClaimSearchCondition.builder()
            .claimNumber(request.claimNumber())
            .policyId(request.policyId())
            .claimantUserId(request.claimantUserId())
            .status(request.status())
            .build();
        Page<InsuranceClaimResult> page = insuranceReader.searchClaims(pageable, condition);
        return ApiPageResponse.of(
            page.getContent().stream().map(this::toClaimResponse).toList(),
            page.getTotalElements(), page.getNumber(), page.getSize()
        );
    }

    @Override
    public void startClaimReview(Long claimId, Long reviewerAdminId) {
        insuranceCommand.startClaimReview(claimId, reviewerAdminId);
    }

    @Override
    public void approveClaim(Long claimId, InsurancePayload.ApproveClaimRequest request) {
        insuranceCommand.approveClaim(new ApproveClaimCommand(
            claimId, request.reviewerAdminId(), request.approvedAmount(), request.reviewNotes()
        ));
    }

    @Override
    public void rejectClaim(Long claimId, InsurancePayload.RejectClaimRequest request) {
        insuranceCommand.rejectClaim(claimId, request.reviewerAdminId(), request.reviewNotes());
    }

    @Override
    public void payClaim(Long claimId, InsurancePayload.PayClaimRequest request) {
        insuranceCommand.payClaim(new PayClaimCommand(claimId, request.referenceId()));
    }

    // ── Mapper ────────────────────────────────────────────────────

    private InsurancePayload.ProductResponse toProductResponse(InsuranceProductResult r) {
        return new InsurancePayload.ProductResponse(
            r.id(), r.productCode(), r.name(), r.description(),
            r.insuranceType(), r.basePremium(), r.premiumFrequency(),
            r.coverageAmount(), r.durationMonths(), r.currency(), r.isActive()
        );
    }

    private InsurancePayload.PolicyResponse toPolicyResponse(InsurancePolicyResult r) {
        List<InsurancePayload.BeneficiaryResponse> bens = r.beneficiaries().stream()
            .map(this::toBeneficiaryResponse).toList();
        return new InsurancePayload.PolicyResponse(
            r.id(), r.policyNumber(), r.productId(), r.userId(), r.insuredUserId(),
            r.billingAccountId(), r.premium(), r.premiumFrequency(), r.coverageAmount(),
            r.currency(), r.startDate(), r.endDate(), r.nextPaymentDate(),
            r.status(), r.activatedAt(), r.terminatedAt(), bens
        );
    }

    private InsurancePayload.BeneficiaryResponse toBeneficiaryResponse(BeneficiaryResult r) {
        return new InsurancePayload.BeneficiaryResponse(
            r.id(), r.beneficiaryUserId(), r.name(), r.relationship(),
            r.sharePercent(), r.beneficiaryType()
        );
    }

    private InsurancePayload.ClaimResponse toClaimResponse(InsuranceClaimResult r) {
        return new InsurancePayload.ClaimResponse(
            r.id(), r.claimNumber(), r.policyId(), r.claimantUserId(),
            r.eventDate(), r.claimReason(), r.claimAmount(), r.approvedAmount(),
            r.payoutAccountId(), r.accountTxId(), r.status(),
            r.submittedAt(), r.reviewedAt(), r.paidAt(),
            r.reviewerAdminId(), r.reviewNotes()
        );
    }
}
