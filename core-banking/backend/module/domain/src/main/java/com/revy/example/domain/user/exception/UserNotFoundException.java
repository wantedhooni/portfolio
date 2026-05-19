package com.revy.example.domain.user.exception;

import com.revy.example.core.error.ErrorCode;

public class UserNotFoundException extends UserException {

    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}
