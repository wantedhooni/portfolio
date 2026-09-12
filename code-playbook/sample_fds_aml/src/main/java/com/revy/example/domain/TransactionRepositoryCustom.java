package com.revy.example.domain;

import com.revy.example.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionRepositoryCustom {
    List<Transaction> findByAccountIdWithinPeriod(UUID accountId, LocalDateTime from, LocalDateTime to);
    BigDecimal sumAmountByAccountIdAndType(UUID accountId, TransactionType type, LocalDateTime from, LocalDateTime to);
    long countByAccountIdWithinHour(UUID accountId, LocalDateTime from);
    List<TransactionSummary> findPotentialStructuring(LocalDate targetDate, BigDecimal threshold);
}

