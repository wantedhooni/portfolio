package com.revy.example.domain.fx.exception;

import com.revy.example.core.error.ErrorCode;

public class FxCorridorNotActiveException extends FxException {

    public FxCorridorNotActiveException(String baseCode, String quoteCode) {
        super(ErrorCode.FX_CORRIDOR_NOT_ACTIVE, "%s/%s".formatted(baseCode, quoteCode));
    }
}
