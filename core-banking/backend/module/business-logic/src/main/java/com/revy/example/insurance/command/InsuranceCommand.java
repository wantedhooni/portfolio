package com.revy.example.insurance.command;

import com.revy.example.insurance.command.dto.ApproveClaimCommand;
import com.revy.example.insurance.command.dto.CreateInsuranceProductCommand;
import com.revy.example.insurance.command.dto.EnrollPolicyCommand;
import com.revy.example.insurance.command.dto.PayClaimCommand;
import com.revy.example.insurance.command.dto.PayPremiumCommand;
import com.revy.example.insurance.command.dto.SubmitClaimCommand;

import java.math.BigDecimal;
import java.time.LocalDate;

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
    /**
     * 보험료 납부 예약 레코드(PremiumPayment) 생성 — 배치 자동이체 전 단계.
     * referenceId 중복 시 IllegalStateException.
     *
     * @return 생성된 PremiumPayment ID
     */
    Long schedulePremiumPayment(Long policyId, BigDecimal amount, String currency,
                                LocalDate dueDate, Long billingAccountId, String referenceId);

    /**
     * 보험료 결제 실행 (자동이체).
     * — Account 차감 → PremiumPayment.markPaid + Policy.advanceNextPaymentDate
     * — 출금 실패 시 PremiumPayment.markFailed (예외 미전파)
     * — 이미 PAID 상태이면 멱등 처리(스킵)
     */
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
