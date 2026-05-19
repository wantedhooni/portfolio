package com.revy.example.domain.user.exception;

import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;

public abstract class UserException extends BusinessException {

    protected UserException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected UserException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}
