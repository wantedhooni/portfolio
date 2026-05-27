package com.revy.example.domain.fx.exception;

import com.revy.example.core.error.ErrorCode;

import java.math.BigDecimal;

public class FxAmountBelowMinException extends FxException {

    public FxAmountBelowMinException(BigDecimal amount, BigDecimal min) {
        super(ErrorCode.FX_AMOUNT_BELOW_MIN, "amount=%s min=%s".formatted(amount, min));
    }
}
