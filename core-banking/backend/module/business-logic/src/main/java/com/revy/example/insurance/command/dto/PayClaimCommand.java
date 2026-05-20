package com.revy.example.insurance.command.dto;

/**
 * 승인된 보험금 지급.
 * 1) referenceId 멱등성 체크
 * 2) payout 계좌로 입금 (AccountTx — 회사 계좌 별도 구현 필요. 데모: DEPOSIT)
 * 3) Claim.markPaid()
 * 4) 분개 자동 생성 — (차) 보험금지급(비용) / (대) 보통예금
 */
public record PayClaimCommand(
        Long claimId,
        String referenceId
) {}
