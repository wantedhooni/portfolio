package com.revy.example.domain.account.enums;

public enum LotStatus {
    OPEN,       // 매수 후 전량 보유 중
    PARTIAL,    // 일부 매도되어 잔량 보유 중
    CLOSED      // 전량 매도 완료
}