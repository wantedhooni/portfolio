package com.revy.example.domain.pg.exception;

import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;

public class PgPaymentNotFoundException extends BusinessException {
    public PgPaymentNotFoundException() { super(ErrorCode.PG_PAYMENT_NOT_FOUND); }
}
