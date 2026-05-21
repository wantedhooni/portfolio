package com.revy.example.order.command.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ExecuteOrderCommand(
        Long       orderId,
        BigDecimal executionPrice,
        BigDecimal fee,
        BigDecimal tax,
        Instant    executedAt
) {}
