package com.revy.example.domain.billing.enums;

public enum InvoiceStatus {
    /** 초안 — 아직 발행 전 */
    DRAFT,
    /** 발행 완료 — 납부 대기 */
    ISSUED,
    /** 납부 완료 */
    PAID,
    /** 기한 초과 */
    OVERDUE,
    /** 취소 */
    CANCELLED,
}
