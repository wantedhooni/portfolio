package com.revy.example.domain.account.exception;

import com.revy.example.core.error.ErrorCode;

import java.math.BigDecimal;


public class InsufficientBalanceException extends AccountException {

    /** 가용잔고와 요청금액을 모두 포함한 상세 생성자 (권장) */
    public InsufficientBalanceException(BigDecimal available, BigDecimal required) {
        super(
                ErrorCode.INSUFFICIENT_BALANCE,
                "available=%s, required=%s".formatted(available, required)
        );
    }

    /** 금액 정보 없이 생성 — 외부 검증 레이어에서 이미 로깅된 경우 */
    public InsufficientBalanceException() {
        super(ErrorCode.INSUFFICIENT_BALANCE);
    }
}