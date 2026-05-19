package com.revy.example.account.reader.dto;

import com.revy.example.domain.account.Account;
import com.revy.example.domain.account.enums.AccountStatus;
import com.revy.example.domain.account.enums.AccountType;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountResult(
        Long id,
        Long userId,
        String accountNumber,
        String accountName,
        AccountType accountType,
        String currency,
        BigDecimal balance,
        BigDecimal availableBalance,
        AccountStatus status,
        Instant createdAt,
        Instant updatedAt
) {

    public static AccountResult from(Account account) {
        return new AccountResult(
            account.getId(),
            account.getUserId(),
            account.getAccountNumber(),
            account.getAccountName(),
            account.getAccountType(),
            account.getCurrency(),
            account.getBalance(),
            account.getAvailableBalance(),
            account.getStatus(),
            account.getCreatedAt(),
            account.getUpdatedAt()
        );
    }
}
