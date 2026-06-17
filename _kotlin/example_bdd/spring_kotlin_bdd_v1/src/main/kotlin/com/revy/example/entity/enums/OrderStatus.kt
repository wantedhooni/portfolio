package com.revy.example.entity.enums

enum class OrderStatus {
    PENDING,      // 주문 대기
    CONFIRMED,    // 주문 확정
    SHIPPED,      // 배송 중
    DELIVERED,    // 배송 완료
    CANCELLED     // 취소
}