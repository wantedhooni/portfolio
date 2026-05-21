package com.revy.example.domain.account.enums;

public enum OrderStatus {
    PENDING,    // 미체결 — 주문 접수, 체결 대기
    FILLED,     // 완전체결
    CANCELLED   // 취소
}
