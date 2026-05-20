package com.revy.example.domain.insurance.exception;

import com.revy.example.core.error.ErrorCode;

public class PolicyNotFoundException extends InsuranceException {

    public PolicyNotFoundException() {
        super(ErrorCode.POLICY_NOT_FOUND);
    }
}
