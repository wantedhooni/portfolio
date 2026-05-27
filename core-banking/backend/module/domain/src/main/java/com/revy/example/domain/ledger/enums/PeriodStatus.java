package com.revy.example.domain.ledger.enums;

import com.revy.example.common.enums.ExposedEnum;

public enum PeriodStatus implements ExposedEnum {
    OPEN,         // 분개 입력 가능
    CLOSED        // 마감 — 신규 분개 차단
}
