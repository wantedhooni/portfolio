package com.revy.example.domain.user.exception;

import com.revy.example.core.error.ErrorCode;

public class EmailAlreadyExistsException extends UserException {

    public EmailAlreadyExistsException() {
        super(ErrorCode.EMAIL_ALREADY_EXISTS);
    }
}
