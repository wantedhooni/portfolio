package com.revy.example.quartz.service.impl;

import com.revy.example.domain.billing.enums.SettlementType;
import com.revy.example.domain.insurance.enums.PaymentStatus;
import com.revy.example.insurance.command.InsuranceCommand;
import com.revy.example.insurance.command.dto.PayPremiumCommand;
import com.revy.example.insurance.reader.InsuranceReader;
import com.revy.example.insurance.reader.dto.InsurancePolicyResult;
import com.revy.example.insurance.reader.dto.PremiumPaymentResult;
import com.revy.example.quartz.service.InsurancePremiumSettlementResult;
import com.revy.example.quartz.service.InsurancePremiumSettlementService;
import com.revy.example.settlement.command.SettlementCommand;
import com.revy.example.settlement.command.dto.CreateSettlementCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 보험료 자동이체 정산 배치 구현체.
 *
 * <p><b>처리 흐름 (계약당):</b>
 * <ol>
 *   <li>referenceId 중복 체크 (멱등성) — 이미 처리된 계약은 SKIP</li>
 *   <li>{@code InsuranceCommand.schedulePremiumPayment} — PremiumPayment(PENDING) 생성</li>
 *   <li>{@code SettlementCommand.create} — Settlement(PENDING) 생성</li>
 *   <li>{@code InsuranceCommand.payPremium} — Account 출금 → PremiumPayment.markPaid/Failed + Policy.advanceNextPaymentDate</li>
 *   <li>납부 결과 확인 후 {@code SettlementCommand.settle/fail}</li>
 * </ol>
 *
 * <p>계약별 {@code REQUIRES_NEW} 트랜잭션으로 개별 실패가 전체 배치를 롤백하지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InsurancePremiumSettlementServiceImpl implements InsurancePremiumSettlementService {

    private final InsuranceReader   insuranceReader;
    private final InsuranceCommand  insuranceCommand;
    private final SettlementCommand settlementCommand;

    /** REQUIRES_NEW 트랜잭션 적용을 위한 self-injection */
    @Autowired
    private InsurancePremiumSettlementServiceImpl self;

    // ── 진입점 ───────────────────────────────────────────────────────────────

    @Override
    public InsurancePremiumSettlementResult execute(LocalDate targetDate) {
        List<InsurancePolicyResult> policies = insuranceReader.findPoliciesDueForBilling(targetDate);
        log.info("[InsurancePremiumSettlement] 시작 targetDate={} 대상건수={}", targetDate, policies.size());

        int successCount = 0;
        int failedCount  = 0;
        int skippedCount = 0;

        for (InsurancePolicyResult policy : policies) {
            try {
                Boolean result = self.processOnePolicy(policy, targetDate);
                if (result == null) {
                    skippedCount++;
                } else if (result) {
                    successCount++;
                } else {
                    failedCount++;
                }
            } catch (Exception e) {
                log.error("[InsurancePremiumSettlement] 처리 오류 policyId={} error={}",
                        policy.id(), e.getMessage(), e);
                failedCount++;
            }
        }

        int total = policies.size();
        log.info("[InsurancePremiumSettlement] 완료 targetDate={} total={} success={} failed={} skipped={}",
                targetDate, total, successCount, failedCount, skippedCount);

        return new InsurancePremiumSettlementResult(targetDate, total, successCount, failedCount);
    }

    // ── 계약 1건 처리 (독립 트랜잭션) ─────────────────────────────────────────

    /**
     * 보험 계약 1건에 대한 보험료 자동이체 + 정산 기록.
     *
     * @return {@code true} = 성공, {@code false} = 출금 실패, {@code null} = 이미 처리됨(멱등 스킵)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Boolean processOnePolicy(InsurancePolicyResult policy, LocalDate targetDate) {
        String referenceId = buildReferenceId(policy.id(), targetDate);

        // 1. 멱등성 체크 — 동일 referenceId로 이미 납부 기록이 있으면 스킵
        if (insuranceReader.existsPaymentByReferenceId(referenceId)) {
            log.info("[InsurancePremiumSettlement] 이미 처리된 계약 스킵 policyId={} referenceId={}",
                    policy.id(), referenceId);
            return null;
        }

        // 2. PremiumPayment(PENDING) 생성
        Long paymentId = insuranceCommand.schedulePremiumPayment(
                policy.id(), policy.premium(), policy.currency(),
                targetDate, policy.billingAccountId(), referenceId
        );
        log.debug("[InsurancePremiumSettlement] PremiumPayment 생성 policyId={} paymentId={}", policy.id(), paymentId);

        // 3. Settlement(PENDING) 생성 — 정산 레코드는 납부 결과와 무관하게 생성
        Long settlementId = settlementCommand.create(new CreateSettlementCommand(
                policy.billingAccountId(),
                SettlementType.INSURANCE_PREMIUM,
                targetDate,
                policy.currency(),
                policy.premium(),       // grossAmount
                BigDecimal.ZERO,        // feeAmount
                BigDecimal.ZERO,        // taxAmount
                policy.premium(),       // netAmount = premium (수수료·세금 없음)
                referenceId,
                "보험료 자동이체 policyId=" + policy.id()
        ));

        // 4. 보험료 자동이체 실행 (출금 실패 시 markFailed — 예외 미전파)
        insuranceCommand.payPremium(new PayPremiumCommand(paymentId, referenceId));

        // 5. 납부 결과 확인 → Settlement 상태 확정
        PremiumPaymentResult payment = insuranceReader.findPaymentById(paymentId)
                .orElseThrow(() -> new IllegalStateException("PremiumPayment 조회 실패 id=" + paymentId));

        if (payment.status() == PaymentStatus.PAID) {
            settlementCommand.settle(settlementId);
            log.info("[InsurancePremiumSettlement] 성공 policyId={} paymentId={} settlementId={}",
                    policy.id(), paymentId, settlementId);
            return true;
        } else {
            String reason = payment.failureReason() != null ? payment.failureReason() : "출금 실패";
            settlementCommand.fail(settlementId, reason);
            log.warn("[InsurancePremiumSettlement] 출금 실패 policyId={} paymentId={} reason={}",
                    policy.id(), paymentId, reason);
            return false;
        }
    }

    // ── 내부 ─────────────────────────────────────────────────────────────────

    /** 멱등성 키: "PREM-{policyId}-{yyyy-MM-dd}" */
    private static String buildReferenceId(Long policyId, LocalDate date) {
        return "PREM-%d-%s".formatted(policyId, date);
    }
}
