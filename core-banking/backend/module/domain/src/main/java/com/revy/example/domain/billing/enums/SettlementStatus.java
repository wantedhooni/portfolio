package com.revy.example.domain.billing.enums;

public enum SettlementStatus {
    /** 정산 대기 */
    PENDING,
    /** 정산 처리 완료 */
    SETTLED,
    /** 정산 실패 */
    FAILED,
    /** 정산 취소 */
    CANCELLED,
}
