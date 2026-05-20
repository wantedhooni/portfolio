package com.revy.example.domain.ledger.exception;

import com.revy.example.core.error.ErrorCode;

public class ClosedPeriodException extends LedgerException {

    public ClosedPeriodException() {
        super(ErrorCode.PERIOD_CLOSED);
    }
}
