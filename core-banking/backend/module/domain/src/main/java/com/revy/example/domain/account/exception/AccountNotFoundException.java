package com.revy.example.domain.account.exception;

import com.revy.example.core.error.ErrorCode;

public class AccountNotFoundException extends AccountException {

    public AccountNotFoundException() {
        super(ErrorCode.ACCOUNT_NOT_FOUND);
    }
}
