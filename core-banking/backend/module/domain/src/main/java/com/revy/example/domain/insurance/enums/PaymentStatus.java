package com.revy.example.domain.insurance.enums;

import com.revy.example.common.enums.ExposedEnum;
public enum PaymentStatus implements ExposedEnum {
    PENDING,      // 납부 예정
    PAID,         // 납부 완료
    OVERDUE,      // 연체
    FAILED,       // 출금 실패
    WAIVED        // 면제
}
