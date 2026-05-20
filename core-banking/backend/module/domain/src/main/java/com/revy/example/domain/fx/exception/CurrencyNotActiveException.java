package com.revy.example.domain.fx.exception;

import com.revy.example.core.error.ErrorCode;

public class CurrencyNotActiveException extends FxException {

    public CurrencyNotActiveException() {
        super(ErrorCode.CURRENCY_NOT_ACTIVE);
    }
}
