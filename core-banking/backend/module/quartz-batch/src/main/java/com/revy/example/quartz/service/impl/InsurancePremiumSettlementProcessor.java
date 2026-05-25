package com.revy.example.quartz.service.impl;

import com.revy.example.domain.billing.enums.SettlementType;
import com.revy.example.domain.insurance.enums.PaymentStatus;
import com.revy.example.insurance.command.InsuranceCommand;
import com.revy.example.insurance.command.dto.PayPremiumCommand;
import com.revy.example.insurance.reader.InsuranceReader;
import com.revy.example.insurance.reader.dto.InsurancePolicyResult;
import com.revy.example.insurance.reader.dto.PremiumPaymentResult;
import com.revy.example.quartz.service.ProcessResult;
import com.revy.example.settlement.command.SettlementCommand;
import com.revy.example.settlement.command.dto.CreateSettlementCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 보험 계약 1건에 대한 보험료 정산을 독립 트랜잭션({@code REQUIRES_NEW})으로 처리하는 컴포넌트입니다.
 *
 * <h3>분리 목적</h3>
 * {@code InsurancePremiumSettlementServiceImpl} 에서 {@code REQUIRES_NEW} 를 적용하려면
 * 동일 빈의 메서드를 self-proxy 를 통해 호출해야 했습니다.
 * self-injection({@code @Autowired private XxxImpl self})은 순환 참조·테스트 어려움·AOP 프록시 깨짐 위험이 있으므로,
 * 독립 빈으로 분리하여 프록시 체인이 정상 동작하도록 합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InsurancePremiumSettlementProcessor {

    private final InsuranceReader   insuranceReader;
    private final InsuranceCommand  insuranceCommand;
    private final SettlementCommand settlementCommand;

    /**
     * 보험 계약 1건에 대한 보험료 자동이체 + 정산 기록을 독립 트랜잭션으로 처리합니다.
     *
     * <p>이 메서드가 커밋/롤백되어도 호출자의 트랜잭션에 영향을 주지 않습니다.</p>
     *
     * @return {@link ProcessResult#SUCCESS} / {@link ProcessResult#FAILED} / {@link ProcessResult#SKIPPED}
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ProcessResult process(InsurancePolicyResult policy, LocalDate targetDate) {
        String referenceId = buildReferenceId(policy.id(), targetDate);

        // 1. 멱등성 체크 — 동일 referenceId 로 이미 납부 기록이 있으면 스킵
        if (insuranceReader.existsPaymentByReferenceId(referenceId)) {
            log.info("[PremiumProcessor] 이미 처리된 계약 스킵 policyId={} referenceId={}",
                    policy.id(), referenceId);
            return ProcessResult.SKIPPED;
        }

        // 2. PremiumPayment(PENDING) 생성
        Long paymentId = insuranceCommand.schedulePremiumPayment(
                policy.id(), policy.premium(), policy.currency(),
                targetDate, policy.billingAccountId(), referenceId
        );

        // 3. Settlement(PENDING) 생성 — 납부 결과와 무관하게 정산 레코드 선생성
        Long settlementId = settlementCommand.create(new CreateSettlementCommand(
                policy.billingAccountId(),
                SettlementType.INSURANCE_PREMIUM,
                targetDate,
                policy.currency(),
                policy.premium(),       // grossAmount
                BigDecimal.ZERO,        // feeAmount
                BigDecimal.ZERO,        // taxAmount
                policy.premium(),       // netAmount
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
            log.info("[PremiumProcessor] 성공 policyId={} paymentId={} settlementId={}",
                    policy.id(), paymentId, settlementId);
            return ProcessResult.SUCCESS;
        } else {
            String reason = payment.failureReason() != null ? payment.failureReason() : "출금 실패";
            settlementCommand.fail(settlementId, reason);
            log.warn("[PremiumProcessor] 출금 실패 policyId={} reason={}", policy.id(), reason);
            return ProcessResult.FAILED;
        }
    }

    /** 멱등성 키: "PREM-{policyId}-{yyyy-MM-dd}" */
    private static String buildReferenceId(Long policyId, LocalDate date) {
        return "PREM-%d-%s".formatted(policyId, date);
    }
}
