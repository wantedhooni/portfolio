package com.revy.api_server.application.exception;

import com.revy.common.error.ApiException;

public class AccountException extends ApiException {
    public AccountException(String code, String message) {
        super(code, message);
    }

    public AccountException(AccountErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
    }

    public AccountException(AccountErrorCode errorCode, String message) {
        super(errorCode.getCode(), message);
    }

    public enum AccountErrorCode  {
        ACCOUNT_NOT_FOUND("ACCOUNT_001", "Account not found"),
        INSUFFICIENT_FUNDS("ACCOUNT_002", "Insufficient funds"),
        ACCOUNT_ALREADY_EXISTS("ACCOUNT_003", "Account already exists"),
        ACCOUNT_NOT_ACTIVE("ACCOUNT_004", "Account is not active"),
        TO_ACCOUNT_NOT_ACTIVE("ACCOUNT_005", "To Account is not active"),
        ACCOUNT_CURRENCY_MISMATCH("ACCOUNT_006", "Account currency mismatch"),
        ;
        private final String code;
        private final String message;

        AccountErrorCode(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}

