package com.revy.example.domain.ledger.exception;

import com.revy.example.core.error.ErrorCode;

public class JournalEntryNotFoundException extends LedgerException {

    public JournalEntryNotFoundException() {
        super(ErrorCode.JOURNAL_ENTRY_NOT_FOUND);
    }
}
