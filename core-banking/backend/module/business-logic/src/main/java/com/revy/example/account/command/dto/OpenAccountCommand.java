package com.revy.example.account.command.dto;

import com.revy.example.domain.account.enums.AccountType;

public record OpenAccountCommand(
        Long userId,
        String accountName,
        AccountType accountType,
        String currency
) {}
