package com.revy.example.trade.command.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record SellCommand(
        Long accountId,
        Long stockId,
        BigDecimal quantity,
        BigDecimal price,
        BigDecimal fee,
        BigDecimal tax,
        String referenceId,
        Instant tradedAt
) {

    public BigDecimal netProceeds() {
        return price.multiply(quantity).subtract(fee).subtract(tax);
    }
}
