package com.revy.example.domain.account.exception;

import com.revy.example.core.error.ErrorCode;

import java.math.BigDecimal;

public class InsufficientPositionException extends PositionException {

    /**
     * 종목·보유·요청 수량을 모두 포함한 상세 생성자 (권장)
     */
    public InsufficientPositionException(Long stockId,
                                         BigDecimal held,
                                         BigDecimal requested) {
        super(
                ErrorCode.INSUFFICIENT_POSITION,
                "stockId=%d, held=%s, requested=%s".formatted(stockId, held, requested)
        );
    }

    /**
     * 로트 단위 처분 실패 — 로트 ID 포함
     */
//    public InsufficientPositionException(Long lotId,
//                                         BigDecimal remaining,
//                                         BigDecimal disposing) {
//        super(
//                ErrorCode.INVALID_LOT_DISPOSAL,
//                "lotId=%d, remaining=%s, disposing=%s".formatted(lotId, remaining, disposing)
//        );
//    }

    public InsufficientPositionException() {
        super(ErrorCode.INSUFFICIENT_POSITION);
    }
}