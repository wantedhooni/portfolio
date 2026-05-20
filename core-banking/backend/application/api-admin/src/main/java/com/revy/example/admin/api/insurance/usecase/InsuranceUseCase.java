package com.revy.example.admin.api.insurance.usecase;

import com.revy.example.admin.api.insurance.payload.InsurancePayload;
import com.revy.example.core.common.ApiPageResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface InsuranceUseCase {

    // ── Product ──────────────────────────────────────────────────
    InsurancePayload.ProductResponse createProduct(InsurancePayload.CreateProductRequest request);
    InsurancePayload.ProductResponse getProduct(Long id);
    ApiPageResponse<InsurancePayload.ProductResponse> searchProducts(Pageable pageable,
                                                                     InsurancePayload.ProductSearchRequest request);
    void updateProductPricing(Long id, InsurancePayload.UpdateProductPricingRequest request);
    void discontinueProduct(Long id);

    // ── Policy ───────────────────────────────────────────────────
    InsurancePayload.PolicyResponse enrollPolicy(InsurancePayload.EnrollPolicyRequest request);
    InsurancePayload.PolicyResponse getPolicy(Long id);
    ApiPageResponse<InsurancePayload.PolicyResponse> searchPolicies(Pageable pageable,
                                                                    InsurancePayload.PolicySearchRequest request);
    void activatePolicy(Long id);
    void suspendPolicy(Long id);
    void reactivatePolicy(Long id);
    void terminatePolicy(Long id);
    void cancelPolicy(Long id);

    // ── Premium Payment ──────────────────────────────────────────
    void payPremium(Long policyId, InsurancePayload.PayPremiumRequest request);
    void markPremiumOverdue(Long paymentId);

    // ── Claim ────────────────────────────────────────────────────
    InsurancePayload.ClaimResponse submitClaim(InsurancePayload.SubmitClaimRequest request);
    InsurancePayload.ClaimResponse getClaim(Long id);
    ApiPageResponse<InsurancePayload.ClaimResponse> searchClaims(Pageable pageable,
                                                                 InsurancePayload.ClaimSearchRequest request);
    void startClaimReview(Long claimId, Long reviewerAdminId);
    void approveClaim(Long claimId, InsurancePayload.ApproveClaimRequest request);
    void rejectClaim(Long claimId, InsurancePayload.RejectClaimRequest request);
    void payClaim(Long claimId, InsurancePayload.PayClaimRequest request);
}
