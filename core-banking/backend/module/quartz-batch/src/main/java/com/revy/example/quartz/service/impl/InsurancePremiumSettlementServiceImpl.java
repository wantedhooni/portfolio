package com.revy.example.quartz.service.impl;

import com.revy.example.insurance.reader.InsuranceReader;
import com.revy.example.insurance.reader.dto.InsurancePolicyResult;
import com.revy.example.quartz.service.InsurancePremiumSettlementResult;
import com.revy.example.quartz.service.InsurancePremiumSettlementService;
import com.revy.example.quartz.service.ProcessResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
 * <p>계약별 {@code REQUIRES_NEW} 트랜잭션은 {@link InsurancePremiumSettlementProcessor}가
 * 독립 빈으로 분리하여 담당합니다. self-injection 없이 AOP 프록시가 정상 동작합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InsurancePremiumSettlementServiceImpl implements InsurancePremiumSettlementService {

    private final InsuranceReader                      insuranceReader;
    private final InsurancePremiumSettlementProcessor  processor;

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
                ProcessResult result = processor.process(policy, targetDate);
                switch (result) {
                    case SUCCESS -> successCount++;
                    case FAILED  -> failedCount++;
                    case SKIPPED -> skippedCount++;
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
}
