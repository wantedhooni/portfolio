package com.revy.example.domain.fx.exception;

import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;

public abstract class FxException extends BusinessException {

    protected FxException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected FxException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}
