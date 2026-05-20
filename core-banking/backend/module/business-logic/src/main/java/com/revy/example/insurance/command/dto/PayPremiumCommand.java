package com.revy.example.insurance.command.dto;

/**
 * 보험료 자동이체 실행.
 * 1) referenceId 멱등성 체크
 * 2) billing 계좌에서 출금 (AccountTx.WITHDRAWAL)
 * 3) PremiumPayment.markPaid()
 * 4) Policy.advanceNextPaymentDate()
 * 5) 분개 자동 생성 — (차) 보통예금 / (대) 보험료수익
 */
public record PayPremiumCommand(
        Long paymentId,
        String referenceId
) {}
