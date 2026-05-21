package com.revy.example.domain.account.exception;

import com.revy.example.core.error.ErrorCode;

public class OrderNotPendingException extends OrderException {
    public OrderNotPendingException() { super(ErrorCode.ORDER_NOT_PENDING); }
}
