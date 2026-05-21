package com.revy.example.domain.billing.exception;

import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;

public class BillingException extends BusinessException {
    public BillingException(ErrorCode errorCode) {
        super(errorCode);
    }
}
