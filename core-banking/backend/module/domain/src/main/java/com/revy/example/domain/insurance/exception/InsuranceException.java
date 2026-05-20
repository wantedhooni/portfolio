package com.revy.example.domain.insurance.exception;

import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;

public abstract class InsuranceException extends BusinessException {

    protected InsuranceException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected InsuranceException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}
