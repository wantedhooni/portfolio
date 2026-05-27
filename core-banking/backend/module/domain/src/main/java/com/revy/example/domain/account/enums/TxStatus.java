package com.revy.example.domain.account.enums;

import com.revy.example.common.enums.ExposedEnum;

public enum TxStatus implements ExposedEnum {
    PENDING,    // 처리 대기 중 (주문 접수 → 체결 전)
    COMPLETED,  // 체결·처리 완료
    CANCELLED,  // 주문 취소
    FAILED      // 처리 실패 (시스템 오류 등)
}