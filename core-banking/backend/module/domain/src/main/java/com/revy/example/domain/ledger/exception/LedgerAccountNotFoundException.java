package com.revy.example.domain.ledger.exception;

import com.revy.example.core.error.ErrorCode;

public class LedgerAccountNotFoundException extends LedgerException {

    public LedgerAccountNotFoundException() {
        super(ErrorCode.LEDGER_ACCOUNT_NOT_FOUND);
    }

    public LedgerAccountNotFoundException(String code) {
        super(ErrorCode.LEDGER_ACCOUNT_NOT_FOUND, "code=" + code);
    }
}
