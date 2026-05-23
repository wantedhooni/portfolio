package com.revy.example.quartz.service;

import java.time.LocalDate;

/**
 * 보험료 자동이체 정산 배치 서비스.
 *
 * <p>납부일이 {@code targetDate} 인 ACTIVE 계약을 조회하여
 * 계약별로 출금(AccountTx) → PremiumPayment 기록 → Settlement 기록을 수행한다.
 * 각 계약은 독립 트랜잭션({@code REQUIRES_NEW})으로 처리되므로
 * 개별 실패가 전체 배치를 롤백하지 않는다.
 */
public interface InsurancePremiumSettlementService {

    InsurancePremiumSettlementResult execute(LocalDate targetDate);
}
