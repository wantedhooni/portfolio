package com.revy.example.domain.account.exception;

import com.revy.example.core.error.ErrorCode;

public class SameAccountTransferException extends AccountException {
    public SameAccountTransferException() {
        super(ErrorCode.SAME_ACCOUNT_TRANSFER);
    }
}
