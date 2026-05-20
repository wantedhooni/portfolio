package com.revy.example.domain.fx.exception;

import com.revy.example.core.error.ErrorCode;

public class CurrencyNotFoundException extends FxException {

    public CurrencyNotFoundException() {
        super(ErrorCode.CURRENCY_NOT_FOUND);
    }

    public CurrencyNotFoundException(String code) {
        super(ErrorCode.CURRENCY_NOT_FOUND, "code=" + code);
    }
}
