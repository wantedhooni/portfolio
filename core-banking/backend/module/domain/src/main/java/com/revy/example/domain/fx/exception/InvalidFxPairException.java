package com.revy.example.domain.fx.exception;

import com.revy.example.core.error.ErrorCode;

public class InvalidFxPairException extends FxException {

    public InvalidFxPairException() {
        super(ErrorCode.INVALID_FX_PAIR);
    }

    public InvalidFxPairException(String detail) {
        super(ErrorCode.INVALID_FX_PAIR, detail);
    }
}
