package com.revy.example.domain.insurance.enums;

public enum ClaimStatus {
    SUBMITTED,    // 청구 접수
    REVIEWING,    // 심사 중
    APPROVED,     // 승인 (지급 대기)
    REJECTED,     // 거절
    PAID          // 지급 완료
}
