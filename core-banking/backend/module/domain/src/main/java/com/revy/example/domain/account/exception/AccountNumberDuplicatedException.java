package com.revy.example.domain.account.exception;

import com.revy.example.core.error.ErrorCode;

public class AccountNumberDuplicatedException extends AccountException {

    public AccountNumberDuplicatedException() {
        super(ErrorCode.ACCOUNT_NUMBER_DUPLICATED);
    }

    public AccountNumberDuplicatedException(String accountNumber) {
        super(ErrorCode.ACCOUNT_NUMBER_DUPLICATED, "accountNumber=%s".formatted(accountNumber));
    }
}
