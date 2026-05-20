package com.revy.example.domain.insurance.exception;

import com.revy.example.core.error.ErrorCode;

import java.math.BigDecimal;

public class InvalidBeneficiaryShareException extends InsuranceException {

    public InvalidBeneficiaryShareException(BigDecimal actualSum) {
        super(ErrorCode.BENEFICIARY_SHARE_INVALID, "sum=" + actualSum);
    }
}
