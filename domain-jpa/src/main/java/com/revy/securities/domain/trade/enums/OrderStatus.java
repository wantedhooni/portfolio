package com.revy.securities.domain.trade.enums;

/**
 * 주문 상태
 */
public enum OrderStatus {
    NEW,               // 주문 생성(접수 완료)
    PARTIALLY_FILLED,  // 일부 체결됨
    FILLED,            // 전량 체결 완료
    CANCELED,          // 사용자/시스템에 의해 취소됨
    REJECTED           // 검증/리스크/거래소 사유로 거절됨
    ;

    public boolean isCancleable() {
        return this == NEW || this == PARTIALLY_FILLED;
    }
}
