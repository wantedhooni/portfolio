package com.revy.example.domain.insurance.enums;

public enum PolicyStatus {
    PENDING,      // 청약 접수
    ACTIVE,       // 효력 발생
    SUSPENDED,    // 효력 정지 (보험료 연체 등)
    TERMINATED,   // 해지
    EXPIRED,      // 만기
    CANCELLED     // 청약 철회
}
