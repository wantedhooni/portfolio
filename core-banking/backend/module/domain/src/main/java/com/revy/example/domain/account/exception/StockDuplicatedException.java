package com.revy.example.domain.account.exception;

import com.revy.example.core.error.ErrorCode;

public class StockDuplicatedException extends StockException {

    public StockDuplicatedException() {
        super(ErrorCode.STOCK_DUPLICATED);
    }

    public StockDuplicatedException(String ticker, String exchange) {
        super(ErrorCode.STOCK_DUPLICATED, "ticker=%s, exchange=%s".formatted(ticker, exchange));
    }
}
