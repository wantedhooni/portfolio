package com.revy.example.account.command.dto;

import java.math.BigDecimal;

/**
 * 계좌이체 — 같은 통화의 두 계좌 간 자금 이동.
 *
 * 흐름:
 * 1) referenceId 멱등성 체크 (이미 처리된 거래면 skip)
 * 2) 출금/입금 계좌가 다른지 검증
 * 3) 두 계좌 모두 ACTIVE, 통화 일치 검증
 * 4) 출금측: amount + fee 차감, AccountTx(TRANSFER_OUT) 기록
 * 5) 입금측: amount 입금, AccountTx(TRANSFER_IN) 기록
 * 6) 두 거래는 동일한 referenceId로 연결됨 (감사·정산 추적)
 *
 * 동일 트랜잭션 내 처리 — 한쪽 실패 시 전체 롤백.
 * 다른 통화 환전이 필요하면 FxCommand.convertCurrency() 사용.
 */
public record TransferCommand(
        Long fromAccountId,
        Long toAccountId,
        BigDecimal amount,
        BigDecimal fee,         // 출금 계좌에서 추가 차감 (0 가능)
        String referenceId      // 멱등성 키
) {}
