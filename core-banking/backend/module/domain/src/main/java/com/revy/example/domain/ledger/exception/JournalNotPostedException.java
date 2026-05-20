package com.revy.example.domain.ledger.exception;

import com.revy.example.core.error.ErrorCode;

public class JournalNotPostedException extends LedgerException {

    public JournalNotPostedException() {
        super(ErrorCode.JOURNAL_NOT_POSTED);
    }
}
