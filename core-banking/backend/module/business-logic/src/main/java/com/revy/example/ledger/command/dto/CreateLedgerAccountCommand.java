package com.revy.example.ledger.command.dto;

import com.revy.example.domain.ledger.enums.AccountCategory;

public record CreateLedgerAccountCommand(
        String accountCode,
        String name,
        AccountCategory category,
        String currency,
        Long parentId,
        String description
) {}
