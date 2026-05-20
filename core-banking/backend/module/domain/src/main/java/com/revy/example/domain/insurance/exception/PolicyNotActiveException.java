package com.revy.example.domain.insurance.exception;

import com.revy.example.core.error.ErrorCode;

public class PolicyNotActiveException extends InsuranceException {

    public PolicyNotActiveException() {
        super(ErrorCode.POLICY_NOT_ACTIVE);
    }
}
