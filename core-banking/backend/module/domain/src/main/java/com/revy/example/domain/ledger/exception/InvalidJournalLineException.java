package com.revy.example.domain.ledger.exception;

import com.revy.example.core.error.ErrorCode;

import java.math.BigDecimal;

public class InvalidJournalLineException extends LedgerException {

    public InvalidJournalLineException() {
        super(ErrorCode.JOURNAL_LINE_INVALID);
    }

    public InvalidJournalLineException(BigDecimal debit, BigDecimal credit) {
        super(ErrorCode.JOURNAL_LINE_INVALID, "debit=%s, credit=%s".formatted(debit, credit));
    }
}
