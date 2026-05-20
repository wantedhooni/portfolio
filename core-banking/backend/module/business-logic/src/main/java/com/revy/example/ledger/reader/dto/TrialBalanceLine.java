package com.revy.example.ledger.reader.dto;

import com.revy.example.domain.ledger.enums.AccountCategory;
import com.revy.example.domain.ledger.enums.NormalBalance;

import java.math.BigDecimal;

/**
 * 시산표 라인 — 계정과목별 차변/대변 합계.
 * 회계 마감 전 검증용: Σ debit = Σ credit (전 계정 합산).
 */
public record TrialBalanceLine(
        Long ledgerAccountId,
        String accountCode,
        String accountName,
        AccountCategory category,
        NormalBalance normalBalance,
        BigDecimal totalDebit,
        BigDecimal totalCredit,
        BigDecimal balance         // normalBalance에 따른 잔액 (positive 표시)
) {}
