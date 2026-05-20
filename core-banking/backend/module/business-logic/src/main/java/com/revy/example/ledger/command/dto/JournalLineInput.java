package com.revy.example.ledger.command.dto;

import java.math.BigDecimal;

/**
 * 분개 라인 입력. debit 또는 credit 중 하나만 양수.
 */
public record JournalLineInput(
        Long ledgerAccountId,
        BigDecimal debit,
        BigDecimal credit,
        String currency,
        String description
) {
    public static JournalLineInput debit(Long ledgerAccountId, BigDecimal amount, String currency, String description) {
        return new JournalLineInput(ledgerAccountId, amount, BigDecimal.ZERO, currency, description);
    }

    public static JournalLineInput credit(Long ledgerAccountId, BigDecimal amount, String currency, String description) {
        return new JournalLineInput(ledgerAccountId, BigDecimal.ZERO, amount, currency, description);
    }
}
