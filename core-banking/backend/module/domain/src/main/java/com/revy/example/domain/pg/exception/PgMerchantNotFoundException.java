package com.revy.example.domain.pg.exception;

import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;

public class PgMerchantNotFoundException extends BusinessException {
    public PgMerchantNotFoundException() { super(ErrorCode.PG_MERCHANT_NOT_FOUND); }
}
