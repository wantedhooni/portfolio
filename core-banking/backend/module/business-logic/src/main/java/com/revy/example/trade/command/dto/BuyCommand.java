package com.revy.example.trade.command.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record BuyCommand(
        Long accountId,
        Long stockId,
        BigDecimal quantity,
        BigDecimal price,
        BigDecimal fee,
        BigDecimal tax,
        String referenceId,
        Instant tradedAt
) {

    public BigDecimal totalCost() {
        return price.multiply(quantity).add(fee).add(tax);
    }
}
