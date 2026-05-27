package com.revy.example.domain.fx.exception;

import com.revy.example.core.error.ErrorCode;

public class FxCorridorDuplicatedException extends FxException {

    public FxCorridorDuplicatedException(String baseCode, String quoteCode) {
        super(ErrorCode.FX_CORRIDOR_DUPLICATED, "%s/%s".formatted(baseCode, quoteCode));
    }
}
