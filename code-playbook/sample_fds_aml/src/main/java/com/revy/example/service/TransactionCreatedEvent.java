package com.revy.example.service;

// 거래 생성 이벤트
public record TransactionCreatedEvent(
    UUID transactionId,
    UUID accountId,
    UUID customerId,
    BigDecimal amount,
    String transactionType,
    LocalDateTime occurredAt
) {}

// FDS 알림 이벤트
public record FdsAlertCreatedEvent(
    UUID alertId,
    UUID transactionId,
    UUID accountId,
    String ruleCode,
    String severity,
    int score
) {}

// AML 케이스 생성 이벤트
public record AmlCaseCreatedEvent(
    UUID caseId,
    UUID customerId,
    String caseType,
    BigDecimal totalAmount,
    LocalDate reportDeadline
) {}