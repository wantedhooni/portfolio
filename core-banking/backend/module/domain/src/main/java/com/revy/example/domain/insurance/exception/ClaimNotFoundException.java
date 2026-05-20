package com.revy.example.domain.insurance.exception;

import com.revy.example.core.error.ErrorCode;

public class ClaimNotFoundException extends InsuranceException {

    public ClaimNotFoundException() {
        super(ErrorCode.CLAIM_NOT_FOUND);
    }
}
