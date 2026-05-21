package com.revy.example.domain.billing.enums;

public enum BillingItemType {
    /** 계좌 유지 수수료 */
    ACCOUNT_FEE,
    /** 주식 거래 수수료 */
    TRADE_COMMISSION,
    /** 외환 스프레드 수수료 */
    FX_SPREAD_FEE,
    /** 계좌이체 수수료 */
    TRANSFER_FEE,
    /** 보험료 */
    INSURANCE_PREMIUM,
    /** 기타 서비스 수수료 */
    SERVICE_FEE,
}
