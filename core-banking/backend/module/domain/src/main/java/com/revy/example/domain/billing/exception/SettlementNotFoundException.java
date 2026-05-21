package com.revy.example.domain.billing.exception;

import com.revy.example.core.error.ErrorCode;

public class SettlementNotFoundException extends BillingException {
    public SettlementNotFoundException() { super(ErrorCode.SETTLEMENT_NOT_FOUND); }
}
