package com.revy.example.domain.fx.exception;

import com.revy.example.core.error.ErrorCode;

public class ExchangeRateNotFoundException extends FxException {

    public ExchangeRateNotFoundException() {
        super(ErrorCode.EXCHANGE_RATE_NOT_FOUND);
    }

    public ExchangeRateNotFoundException(String base, String quote) {
        super(ErrorCode.EXCHANGE_RATE_NOT_FOUND, "%s/%s".formatted(base, quote));
    }
}
