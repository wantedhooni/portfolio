package com.revy.example.domain.insurance.exception;

import com.revy.example.core.error.ErrorCode;

import java.math.BigDecimal;

public class ClaimAmountExceedsCoverageException extends InsuranceException {

    public ClaimAmountExceedsCoverageException(BigDecimal claim, BigDecimal coverage) {
        super(ErrorCode.CLAIM_AMOUNT_EXCEEDS_COVERAGE,
              "claim=%s, coverage=%s".formatted(claim, coverage));
    }
}
