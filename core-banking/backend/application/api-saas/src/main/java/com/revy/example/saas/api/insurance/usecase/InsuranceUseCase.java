package com.revy.example.saas.api.insurance.usecase;

import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.saas.api.insurance.payload.InsurancePayload;
import org.springframework.data.domain.Pageable;

public interface InsuranceUseCase {

    // ── Product (public) ─────────────────────────────────────────
    ApiPageResponse<InsurancePayload.ProductResponse> searchProducts(Pageable pageable,
                                                                     InsurancePayload.ProductSearchRequest request);
    InsurancePayload.ProductResponse getProduct(Long id);

    // ── Policy (mine) ────────────────────────────────────────────
    ApiPageResponse<InsurancePayload.PolicyResponse> myPolicies(Long userId, Pageable pageable);
    InsurancePayload.PolicyResponse getMyPolicy(Long userId, Long policyId);
    InsurancePayload.PolicyResponse enroll(Long userId, InsurancePayload.EnrollRequest request);

    // ── Claim (mine) ─────────────────────────────────────────────
    ApiPageResponse<InsurancePayload.ClaimResponse> myClaims(Long userId, Pageable pageable);
    InsurancePayload.ClaimResponse getMyClaim(Long userId, Long claimId);
    InsurancePayload.ClaimResponse submitClaim(Long userId, InsurancePayload.SubmitClaimRequest request);
}
