package com.revy.securities.domain.trade.enums;

/**
 * 주문 유효 기간(Time In Force)
 */
public enum TimeInForce {
    DAY, // 당일 장 종료 시 만료
    GTC, // 취소할 때까지 유지
    IOC, // 즉시 체결 가능한 만큼만 체결, 나머지 취소
    FOK  // 전량 즉시 체결 아니면 전부 취소
}

