package com.revy.example.domain.ledger.exception;

import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;

public abstract class LedgerException extends BusinessException {

    protected LedgerException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected LedgerException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}
