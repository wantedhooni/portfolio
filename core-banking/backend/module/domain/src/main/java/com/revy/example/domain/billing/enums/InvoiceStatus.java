package com.revy.example.domain.billing.enums;

import com.revy.example.common.enums.ExposedEnum;

public enum InvoiceStatus implements ExposedEnum {
    /**
     * 초안 — 아직 발행 전
     */
    DRAFT,
    /**
     * 발행 완료 — 납부 대기
     */
    ISSUED,
    /**
     * 납부 완료
     */
    PAID,
    /**
     * 기한 초과
     */
    OVERDUE,
    /**
     * 취소
     */
    CANCELLED,
}
