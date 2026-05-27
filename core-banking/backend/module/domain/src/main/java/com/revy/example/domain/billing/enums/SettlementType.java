package com.revy.example.domain.billing.enums;

import com.revy.example.common.enums.ExposedEnum;

public enum SettlementType implements ExposedEnum {
    /** 주식 체결 정산 */
    TRADE,
    /** 외환 거래 정산 */
    FX,
    /** 보험료 정산 */
    INSURANCE_PREMIUM,
    /** 수수료 정산 */
    FEE,
}
