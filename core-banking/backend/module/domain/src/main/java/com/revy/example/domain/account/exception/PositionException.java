package com.revy.example.domain.account.exception;

import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;

public abstract class PositionException extends BusinessException {
    protected PositionException(ErrorCode errorCode) {
        super(errorCode);
    }
    protected PositionException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}