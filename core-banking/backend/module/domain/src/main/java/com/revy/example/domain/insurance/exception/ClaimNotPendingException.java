package com.revy.example.domain.insurance.exception;

import com.revy.example.core.error.ErrorCode;

public class ClaimNotPendingException extends InsuranceException {

    public ClaimNotPendingException() {
        super(ErrorCode.CLAIM_NOT_PENDING);
    }
}
