package com.revy.example.ledger.reader.dto;

import com.revy.example.domain.ledger.JournalLine;

import java.math.BigDecimal;

public record JournalLineResult(
        Long id,
        Integer lineNo,
        Long ledgerAccountId,
        BigDecimal debit,
        BigDecimal credit,
        String currency,
        String description
) {
    public static JournalLineResult from(JournalLine l) {
        return new JournalLineResult(l.getId(), l.getLineNo(), l.getLedgerAccountId(),
                                     l.getDebit(), l.getCredit(), l.getCurrency(), l.getDescription());
    }
}
