package com.revy.example.domain.insurance.exception;

import com.revy.example.core.error.ErrorCode;

public class PolicyExpiredException extends InsuranceException {

    public PolicyExpiredException() {
        super(ErrorCode.POLICY_EXPIRED);
    }
}
