package com.revy.example.domain.ledger.exception;

import com.revy.example.core.error.ErrorCode;

import java.math.BigDecimal;

public class UnbalancedJournalEntryException extends LedgerException {

    public UnbalancedJournalEntryException(BigDecimal debitSum, BigDecimal creditSum) {
        super(ErrorCode.UNBALANCED_JOURNAL_ENTRY,
              "debit=%s, credit=%s".formatted(debitSum, creditSum));
    }
}
