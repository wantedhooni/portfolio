package com.revy.example.domain.account.exception;

import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;

public abstract class OrderException extends BusinessException {
    protected OrderException(ErrorCode errorCode) { super(errorCode); }
    protected OrderException(ErrorCode errorCode, String detail) { super(errorCode, detail); }
}
