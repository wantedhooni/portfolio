package com.revy.example.domain.account.exception;

import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;

public abstract class AccountException extends BusinessException {
    protected AccountException(ErrorCode errorCode) {
        super(errorCode);
    }
    protected AccountException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}