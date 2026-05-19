package com.revy.example.domain.account.exception;

import com.revy.example.core.error.ErrorCode;

public class StockNotFoundException extends StockException {

    public StockNotFoundException() {
        super(ErrorCode.STOCK_NOT_FOUND);
    }
}
