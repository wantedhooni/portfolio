package com.revy.example.trade.command.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record DividendCommand(
        Long accountId,
        Long stockId,
        BigDecimal grossAmount,
        BigDecimal tax,
        String referenceId,
        Instant tradedAt
) {

    public BigDecimal netAmount() {
        return grossAmount.subtract(tax);
    }
}
