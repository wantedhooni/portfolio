package com.revy.example.account.command.dto;

import java.math.BigDecimal;

public record WithdrawCommand(
        Long accountId,
        BigDecimal amount,
        String referenceId
) {}
