package com.revy.example.domain.ledger.exception;

import com.revy.example.core.error.ErrorCode;

public class JournalAlreadyPostedException extends LedgerException {

    public JournalAlreadyPostedException() {
        super(ErrorCode.JOURNAL_ALREADY_POSTED);
    }
}
