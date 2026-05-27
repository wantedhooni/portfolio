package com.revy.example.domain.account.enums;

import com.revy.example.common.enums.ExposedEnum;
public enum LotStatus implements ExposedEnum {
    OPEN,       // 매수 후 전량 보유 중
    PARTIAL,    // 일부 매도되어 잔량 보유 중
    CLOSED      // 전량 매도 완료
}