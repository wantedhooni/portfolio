package com.revy.example.domain;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionSummary(
    UUID accountId,
    Long transactionCount,
    BigDecimal totalAmount
) {
}
