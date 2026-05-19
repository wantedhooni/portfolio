package com.revy.example.domain.account.exception;

import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;

public abstract class StockException extends BusinessException {

    protected StockException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected StockException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}
