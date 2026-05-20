package com.revy.example.domain.ledger.exception;

import com.revy.example.core.error.ErrorCode;

public class AccountingPeriodNotFoundException extends LedgerException {

    public AccountingPeriodNotFoundException() {
        super(ErrorCode.PERIOD_NOT_FOUND);
    }
}
