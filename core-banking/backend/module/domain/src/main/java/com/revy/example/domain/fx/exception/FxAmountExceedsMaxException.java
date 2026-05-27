package com.revy.example.domain.fx.exception;

import com.revy.example.core.error.ErrorCode;

import java.math.BigDecimal;

public class FxAmountExceedsMaxException extends FxException {

    public FxAmountExceedsMaxException(BigDecimal amount, BigDecimal max) {
        super(ErrorCode.FX_AMOUNT_EXCEEDS_MAX, "amount=%s max=%s".formatted(amount, max));
    }
}
