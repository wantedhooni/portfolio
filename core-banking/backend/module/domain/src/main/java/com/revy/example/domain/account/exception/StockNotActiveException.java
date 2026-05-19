package com.revy.example.domain.account.exception;

import com.revy.example.core.error.ErrorCode;

public class StockNotActiveException extends StockException {

    public StockNotActiveException() {
        super(ErrorCode.STOCK_NOT_ACTIVE);
    }
}
