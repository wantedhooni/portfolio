package com.revy.example.domain.account.exception;

import com.revy.example.core.error.ErrorCode;

public class OrderAlreadyExecutedException extends OrderException {
    public OrderAlreadyExecutedException() { super(ErrorCode.ORDER_ALREADY_EXECUTED); }
}
