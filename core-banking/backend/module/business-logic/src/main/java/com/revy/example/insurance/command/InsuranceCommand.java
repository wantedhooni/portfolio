package com.revy.example.insurance.command;

import com.revy.example.insurance.command.dto.ApproveClaimCommand;
import com.revy.example.insurance.command.dto.CreateInsuranceProductCommand;
import com.revy.example.insurance.command.dto.EnrollPolicyCommand;
import com.revy.example.insurance.command.dto.PayClaimCommand;
import com.revy.example.insurance.command.dto.PayPremiumCommand;
import com.revy.example.insurance.command.dto.SubmitClaimCommand;

import java.math.BigDecimal;

public interface InsuranceCommand {

    // ── Product ──────────────────────────────────────────────────
    Long createProduct(CreateInsuranceProductCommand command);
    void updateProductPricing(Long productId, BigDecimal basePremium, BigDecimal coverageAmount);
    void discontinueProduct(Long productId);

    // ── Policy lifecycle ─────────────────────────────────────────
    Long enrollPolicy(EnrollPolicyCommand command);
    void activatePolicy(Long policyId);
    void suspendPolicy(Long policyId);
    void reactivatePolicy(Long policyId);
    void terminatePolicy(Long policyId);
    void cancelPolicy(Long policyId);

    // ── Premium ──────────────────────────────────────────────────
    /** 보험료 결제 (자동이체) — Account 차감 + Policy 다음 납부일 진행 + 분개 생성 */
    void payPremium(PayPremiumCommand command);

    /** 연체 처리 (배치) */
    void markPremiumOverdue(Long paymentId);

    // ── Claim ────────────────────────────────────────────────────
    Long submitClaim(SubmitClaimCommand command);
    void startClaimReview(Long claimId, Long reviewerAdminId);
    void approveClaim(ApproveClaimCommand command);
    void rejectClaim(Long claimId, Long reviewerAdminId, String reviewNotes);

    /** 보험금 지급 — 고객 계좌 입금 + 분개 생성 */
    void payClaim(PayClaimCommand command);
}
