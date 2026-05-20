package com.revy.example.saas.api.insurance.usecase.impl;

import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.insurance.command.InsuranceCommand;
import com.revy.example.insurance.command.dto.EnrollPolicyCommand;
import com.revy.example.insurance.command.dto.SubmitClaimCommand;
import com.revy.example.insurance.reader.InsuranceReader;
import com.revy.example.insurance.reader.dto.BeneficiaryResult;
import com.revy.example.insurance.reader.dto.InsuranceClaimResult;
import com.revy.example.insurance.reader.dto.InsuranceClaimSearchCondition;
import com.revy.example.insurance.reader.dto.InsurancePolicyResult;
import com.revy.example.insurance.reader.dto.InsurancePolicySearchCondition;
import com.revy.example.insurance.reader.dto.InsuranceProductResult;
import com.revy.example.insurance.reader.dto.InsuranceProductSearchCondition;
import com.revy.example.saas.api.common.AccountOwnershipValidator;
import com.revy.example.saas.api.insurance.payload.InsurancePayload;
import com.revy.example.saas.api.insurance.usecase.InsuranceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InsuranceUseCaseImpl implements InsuranceUseCase {

    private final InsuranceReader insuranceReader;
    private final InsuranceCommand insuranceCommand;
    private final AccountOwnershipValidator ownershipValidator;

    // ── Product ──────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ApiPageResponse<InsurancePayload.ProductResponse> searchProducts(
            Pageable pageable, InsurancePayload.ProductSearchRequest request) {
        InsuranceProductSearchCondition cond = InsuranceProductSearchCondition.builder()
            .productCode(request.productCode())
            .name(request.name())
            .insuranceType(request.insuranceType())
            .currency(request.currency())
            .isActive(true)        // 사용자에게는 활성 상품만 노출
            .build();
        Page<InsuranceProductResult> page = insuranceReader.searchProducts(pageable, cond);
        return ApiPageResponse.of(
            page.getContent().stream().map(this::toProductResponse).toList(),
            page.getTotalElements(), page.getNumber(), page.getSize()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public InsurancePayload.ProductResponse getProduct(Long id) {
        return insuranceReader.findProductById(id)
            .map(this::toProductResponse)
            .orElseThrow(() -> new BusinessException(ErrorCode.INSURANCE_PRODUCT_NOT_FOUND));
    }

    // ── Policy ───────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ApiPageResponse<InsurancePayload.PolicyResponse> myPolicies(Long userId, Pageable pageable) {
        InsurancePolicySearchCondition cond = InsurancePolicySearchCondition.builder()
            .userId(userId)
            .build();
        Page<InsurancePolicyResult> page = insuranceReader.searchPolicies(pageable, cond);
        return ApiPageResponse.of(
            page.getContent().stream().map(this::toPolicyResponse).toList(),
            page.getTotalElements(), page.getNumber(), page.getSize()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public InsurancePayload.PolicyResponse getMyPolicy(Long userId, Long policyId) {
        InsurancePolicyResult p = insuranceReader.findPolicyById(policyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.POLICY_NOT_FOUND));
        if (!p.userId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return toPolicyResponse(p);
    }

    @Override
    public InsurancePayload.PolicyResponse enroll(Long userId, InsurancePayload.EnrollRequest request) {
        // 결제 계좌가 본인 소유인지 검증
        ownershipValidator.requireOwner(userId, request.billingAccountId());

        // 피보험자 — 본인이 직접 가입한 경우 userId, 아니면 명시한 insuredUserId
        Long insuredUserId = request.insuredUserId() == null ? userId : request.insuredUserId();

        List<EnrollPolicyCommand.BeneficiaryInput> beneficiaries = null;
        if (request.beneficiaries() != null) {
            beneficiaries = request.beneficiaries().stream()
                .map(b -> new EnrollPolicyCommand.BeneficiaryInput(
                    b.beneficiaryUserId(), b.name(), b.relationship(), b.sharePercent(), b.type()))
                .toList();
        }

        Long id = insuranceCommand.enrollPolicy(new EnrollPolicyCommand(
            request.productId(),
            userId,                  // 계약자 = JWT 사용자
            insuredUserId,
            request.billingAccountId(),
            request.startDate(),
            beneficiaries
        ));
        return getMyPolicy(userId, id);
    }

    // ── Claim ────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ApiPageResponse<InsurancePayload.ClaimResponse> myClaims(Long userId, Pageable pageable) {
        InsuranceClaimSearchCondition cond = InsuranceClaimSearchCondition.builder()
            .claimantUserId(userId)
            .build();
        Page<InsuranceClaimResult> page = insuranceReader.searchClaims(pageable, cond);
        return ApiPageResponse.of(
            page.getContent().stream().map(this::toClaimResponse).toList(),
            page.getTotalElements(), page.getNumber(), page.getSize()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public InsurancePayload.ClaimResponse getMyClaim(Long userId, Long claimId) {
        InsuranceClaimResult c = insuranceReader.findClaimById(claimId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CLAIM_NOT_FOUND));
        if (!c.claimantUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return toClaimResponse(c);
    }

    @Override
    public InsurancePayload.ClaimResponse submitClaim(Long userId, InsurancePayload.SubmitClaimRequest request) {
        // 본인 증권인지 검증
        InsurancePolicyResult policy = insuranceReader.findPolicyById(request.policyId())
            .orElseThrow(() -> new BusinessException(ErrorCode.POLICY_NOT_FOUND));
        if (!policy.userId().equals(userId) && !policy.insuredUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        // 지급 계좌가 본인 소유인지 검증
        ownershipValidator.requireOwner(userId, request.payoutAccountId());

        Long id = insuranceCommand.submitClaim(new SubmitClaimCommand(
            request.policyId(),
            userId,                  // 청구인 = JWT 사용자
            request.eventDate(),
            request.claimReason(),
            request.claimAmount(),
            request.payoutAccountId()
        ));
        return getMyClaim(userId, id);
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
        List<InsurancePayload.BeneficiaryResponse> bens = r.beneficiaries() == null
            ? List.of()
            : r.beneficiaries().stream().map(this::toBeneficiaryResponse).toList();
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
            r.payoutAccountId(), r.status(),
            r.submittedAt(), r.reviewedAt(), r.paidAt(), r.reviewNotes()
        );
    }
}
