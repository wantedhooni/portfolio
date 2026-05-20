package com.revy.example.domain.account.exception;

import com.revy.example.core.error.ErrorCode;

public class CurrencyMismatchException extends AccountException {
    public CurrencyMismatchException(String from, String to) {
        super(ErrorCode.CURRENCY_MISMATCH, "from=%s, to=%s".formatted(from, to));
    }
}
