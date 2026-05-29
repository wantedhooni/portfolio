package com.revy.example.domain.pg.enums;

import com.revy.example.common.enums.ExposedEnum;

public enum PgPaymentStatus implements ExposedEnum {
    REQUESTED,  // 결제 요청 접수
    APPROVED,   // 승인 완료 (정산 대기)
    CANCELLED,  // 고객 취소
    REFUNDED,   // 환불 완료
    FAILED      // 결제 실패
}
