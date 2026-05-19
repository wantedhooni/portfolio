package com.revy.example.domain.account.exception;

import com.revy.example.core.error.ErrorCode;

public class InvalidLotDisposalException extends PositionException {

    public InvalidLotDisposalException() {
        super(ErrorCode.INVALID_LOT_DISPOSAL);
    }

    public InvalidLotDisposalException(String detail) {
        super(ErrorCode.INVALID_LOT_DISPOSAL, detail);
    }
}
