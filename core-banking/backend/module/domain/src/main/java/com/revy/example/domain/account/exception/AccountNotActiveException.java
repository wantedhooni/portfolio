package com.revy.example.domain.account.exception;

import com.revy.example.domain.account.enums.AccountStatus;

import static com.revy.example.core.error.ErrorCode.ACCOUNT_NOT_ACTIVE;

public class AccountNotActiveException extends AccountException {

    /**
     * 계좌 ID와 현재 상태를 포함 (권장)
     */
    public AccountNotActiveException(Long accountId, AccountStatus currentStatus) {
        super(
                ACCOUNT_NOT_ACTIVE,
                "accountId=%d, status=%s".formatted(accountId, currentStatus)
        );
    }

    public AccountNotActiveException() {
        super(ACCOUNT_NOT_ACTIVE);
    }
}