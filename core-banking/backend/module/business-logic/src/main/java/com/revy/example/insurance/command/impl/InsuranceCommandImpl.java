package com.revy.example.insurance.command.impl;

import com.revy.example.account.command.AccountCommand;
import com.revy.example.account.command.dto.DepositCommand;
import com.revy.example.account.command.dto.WithdrawCommand;
import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.domain.insurance.InsuranceClaim;
import com.revy.example.domain.insurance.InsurancePolicy;
import com.revy.example.domain.insurance.InsuranceProduct;
import com.revy.example.domain.insurance.PremiumPayment;
import com.revy.example.domain.insurance.enums.PaymentStatus;
import com.revy.example.domain.insurance.exception.ClaimNotFoundException;
import com.revy.example.domain.insurance.exception.InsuranceProductNotFoundException;
import com.revy.example.domain.insurance.exception.PolicyNotFoundException;
import com.revy.example.insurance.command.InsuranceCommand;
import com.revy.example.insurance.command.dto.ApproveClaimCommand;
import com.revy.example.insurance.command.dto.CreateInsuranceProductCommand;
import com.revy.example.insurance.command.dto.EnrollPolicyCommand;
import com.revy.example.insurance.command.dto.PayClaimCommand;
import com.revy.example.insurance.command.dto.PayPremiumCommand;
import com.revy.example.insurance.command.dto.SubmitClaimCommand;
import com.revy.example.insurance.reader.InsuranceReader;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class InsuranceCommandImpl implements InsuranceCommand {

    private final EntityManager    entityManager;
    private final InsuranceReader  insuranceReader;
    private final AccountCommand   accountCommand;

    // ── Product ──────────────────────────────────────────────────

    @Override
    public Long createProduct(CreateInsuranceProductCommand command) {
        if (insuranceReader.findProductByCode(command.productCode()).isPresent()) {
            throw new BusinessException(ErrorCode.INSURANCE_PRODUCT_DISCONTINUED, "duplicate code");
        }
        InsuranceProduct p = InsuranceProduct.create(
            command.productCode(), command.name(), command.description(),
            command.insuranceType(), command.basePremium(), command.premiumFrequency(),
            command.coverageAmount(), command.durationMonths(), command.currency()
        );
        entityManager.persist(p);
        // TODO:REVY - EVENT 발행(InsuranceProductCreated) - commit after
        return p.getId();
    }

    @Override
    public void updateProductPricing(Long productId, BigDecimal basePremium, BigDecimal coverageAmount) {
        loadProduct(productId).updatePricing(basePremium, coverageAmount);
        // TODO:REVY - EVENT 발행(InsuranceProductPricingUpdated) - commit after
    }

    @Override
    public void discontinueProduct(Long productId) {
        loadProduct(productId).discontinue();
        // TODO:REVY - EVENT 발행(InsuranceProductDiscontinued) - commit after
    }

    // ── Policy ───────────────────────────────────────────────────

    @Override
    public Long enrollPolicy(EnrollPolicyCommand command) {
        InsuranceProduct product = loadProduct(command.productId());
        if (!product.isActive()) {
            throw new BusinessException(ErrorCode.INSURANCE_PRODUCT_DISCONTINUED);
        }

        String policyNumber = "POL-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        var endDate = command.startDate().plusMonths(product.getDurationMonths());

        InsurancePolicy policy = InsurancePolicy.enroll(
            policyNumber, product.getId(),
            command.userId(), command.insuredUserId(), command.billingAccountId(),
            product.getBasePremium(), product.getPremiumFrequency(),
            product.getCoverageAmount(), product.getCurrency(),
            command.startDate(), endDate
        );

        if (command.beneficiaries() != null) {
            for (var b : command.beneficiaries()) {
                policy.addBeneficiary(b.beneficiaryUserId(), b.name(), b.relationship(),
                                      b.sharePercent(), b.type());
            }
        }

        entityManager.persist(policy);
        // TODO:REVY - EVENT 발행(InsurancePolicyEnrolled) - commit after
        return policy.getId();
    }

    @Override
    public void activatePolicy(Long policyId) {
        loadPolicy(policyId).activate(Instant.now());
        // TODO:REVY - EVENT 발행(InsurancePolicyActivated) - commit after
    }

    @Override
    public void suspendPolicy(Long policyId) {
        loadPolicy(policyId).suspend();
        // TODO:REVY - EVENT 발행(InsurancePolicySuspended) - commit after
    }

    @Override
    public void reactivatePolicy(Long policyId) {
        loadPolicy(policyId).reactivate();
        // TODO:REVY - EVENT 발행(InsurancePolicyReactivated) - commit after
    }

    @Override
    public void terminatePolicy(Long policyId) {
        loadPolicy(policyId).terminate(Instant.now());
        // TODO:REVY - EVENT 발행(InsurancePolicyTerminated) - commit after
    }

    @Override
    public void cancelPolicy(Long policyId) {
        loadPolicy(policyId).cancel();
        // TODO:REVY - EVENT 발행(InsurancePolicyCanceled) - commit after
    }

    // ── Premium ──────────────────────────────────────────────────

    @Override
    public Long schedulePremiumPayment(Long policyId, java.math.BigDecimal amount, String currency,
                                       java.time.LocalDate dueDate, Long billingAccountId, String referenceId) {
        if (insuranceReader.existsPaymentByReferenceId(referenceId)) {
            throw new IllegalStateException("이미 예약된 납부 referenceId=" + referenceId);
        }
        PremiumPayment payment = PremiumPayment.schedule(policyId, amount, currency, dueDate, billingAccountId, referenceId);
        entityManager.persist(payment);
        return payment.getId();
    }

    @Override
    public void payPremium(PayPremiumCommand command) {
        PremiumPayment payment = loadPayment(command.paymentId());

        // 멱등성: 이미 처리된 납부는 스킵
        if (payment.getStatus() == PaymentStatus.PAID) {
            log.info("[payPremium] 이미 납부 완료. paymentId={}", command.paymentId());
            return;
        }

        InsurancePolicy policy = loadPolicy(payment.getPolicyId());
        policy.validateActive();

        // 출금 (자동이체) — 실패 시 예외를 markFailed로 변환, 예외 미전파
        try {
            accountCommand.withdraw(new WithdrawCommand(
                policy.getBillingAccountId(), payment.getAmount(), command.referenceId()
            ));
        } catch (RuntimeException e) {
            log.warn("[payPremium] 출금 실패 policyId={} reason={}", payment.getPolicyId(), e.getMessage());
            payment.markFailed(e.getMessage());
            return;
        }

        // 납부 처리 + 다음 납부일 진행
        payment.markPaid(null, Instant.now());
        policy.advanceNextPaymentDate();
        // TODO:REVY - LedgerCommand: (차) 보통예금 / (대) 보험료수익
        // TODO:REVY - EVENT 발행(InsurancePremiumPaid)
    }

    @Override
    public void markPremiumOverdue(Long paymentId) {
        loadPayment(paymentId).markOverdue();
        // TODO:REVY - EVENT 발행(InsurancePremiumOverdueMarked) - commit after
    }

    // ── Claim ────────────────────────────────────────────────────

    @Override
    public Long submitClaim(SubmitClaimCommand command) {
        InsurancePolicy policy = loadPolicy(command.policyId());
        policy.validateActive();

        String claimNumber = "CLM-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        InsuranceClaim claim = InsuranceClaim.submit(
            claimNumber, command.policyId(), command.claimantUserId(),
            command.eventDate(), command.claimReason(), command.claimAmount(),
            command.payoutAccountId(), Instant.now()
        );
        entityManager.persist(claim);
        // TODO:REVY - EVENT 발행(InsuranceClaimSubmitted) - commit after
        return claim.getId();
    }

    @Override
    public void startClaimReview(Long claimId, Long reviewerAdminId) {
        loadClaim(claimId).startReview(reviewerAdminId);
        // TODO:REVY - EVENT 발행(InsuranceClaimReviewStarted) - commit after
    }

    @Override
    public void approveClaim(ApproveClaimCommand command) {
        InsuranceClaim claim = loadClaim(command.claimId());
        InsurancePolicy policy = loadPolicy(claim.getPolicyId());
        claim.approve(command.approvedAmount(), policy.getCoverageAmount(),
                      command.reviewNotes(), Instant.now());
        // TODO:REVY - EVENT 발행(InsuranceClaimApproved) - commit after
    }

    @Override
    public void rejectClaim(Long claimId, Long reviewerAdminId, String reviewNotes) {
        InsuranceClaim claim = loadClaim(claimId);
        // reviewer가 review를 시작 안 한 경우 자동 startReview
        if (claim.getReviewerAdminId() == null) claim.startReview(reviewerAdminId);
        claim.reject(reviewNotes, Instant.now());
        // TODO:REVY - EVENT 발행(InsuranceClaimRejected) - commit after
    }

    @Override
    public void payClaim(PayClaimCommand command) {
        InsuranceClaim claim = loadClaim(command.claimId());
        if (claim.getApprovedAmount() == null || claim.getApprovedAmount().signum() <= 0) {
            throw new BusinessException(ErrorCode.CLAIM_NOT_PENDING);
        }

        // payout 계좌 입금
        accountCommand.deposit(new DepositCommand(
            claim.getPayoutAccountId(), claim.getApprovedAmount(), command.referenceId()
        ));

        claim.markPaid(null, Instant.now());
        // 분개: (차) 보험금지급(비용) / (대) 보통예금 — 후속 작업
        // TODO:REVY - EVENT 발행(InsuranceClaimPaid) - commit after
    }

    // ── 내부 ─────────────────────────────────────────────────────

    private InsuranceProduct loadProduct(Long id) {
        InsuranceProduct p = entityManager.find(InsuranceProduct.class, id);
        if (p == null) throw new InsuranceProductNotFoundException();
        return p;
    }

    private InsurancePolicy loadPolicy(Long id) {
        InsurancePolicy p = entityManager.find(InsurancePolicy.class, id);
        if (p == null) throw new PolicyNotFoundException();
        return p;
    }

    private PremiumPayment loadPayment(Long id) {
        PremiumPayment p = entityManager.find(PremiumPayment.class, id);
        if (p == null) throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "PremiumPayment id=" + id);
        return p;
    }

    private InsuranceClaim loadClaim(Long id) {
        InsuranceClaim c = entityManager.find(InsuranceClaim.class, id);
        if (c == null) throw new ClaimNotFoundException();
        return c;
    }
}
