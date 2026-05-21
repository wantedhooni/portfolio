package com.revy.example.domain.account.exception;

import com.revy.example.core.error.ErrorCode;

public class OrderNotFoundException extends OrderException {
    public OrderNotFoundException() { super(ErrorCode.ORDER_NOT_FOUND); }
}
