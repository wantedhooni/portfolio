package com.revy.example.account.command.dto;

import java.math.BigDecimal;

public record DepositCommand(
        Long accountId,
        BigDecimal amount,
        String referenceId
) {}
