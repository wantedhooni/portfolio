package com.revy.example.domain.pg.exception;

import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;

public class PgSettlementNotFoundException extends BusinessException {
    public PgSettlementNotFoundException() { super(ErrorCode.PG_SETTLEMENT_NOT_FOUND); }
}
