package com.revy.example.domain.fx.exception;

import com.revy.example.core.error.ErrorCode;

public class FxCorridorNotFoundException extends FxException {

    public FxCorridorNotFoundException(String baseCode, String quoteCode) {
        super(ErrorCode.FX_CORRIDOR_NOT_FOUND, "%s/%s".formatted(baseCode, quoteCode));
    }

    public FxCorridorNotFoundException(Long id) {
        super(ErrorCode.FX_CORRIDOR_NOT_FOUND, "id=" + id);
    }
}
