package com.revy.example.domain.insurance.exception;

import com.revy.example.core.error.ErrorCode;

public class InsuranceProductNotFoundException extends InsuranceException {

    public InsuranceProductNotFoundException() {
        super(ErrorCode.INSURANCE_PRODUCT_NOT_FOUND);
    }
}
