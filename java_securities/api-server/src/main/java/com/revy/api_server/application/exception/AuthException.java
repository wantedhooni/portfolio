package com.revy.api_server.application.exception;

import com.revy.common.error.ApiException;
import com.revy.common.error.ErrorCode;

public class AuthException extends ApiException {
    public AuthException(String code, String message) {
        super(code, message);
    }

    public AuthException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AuthException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
