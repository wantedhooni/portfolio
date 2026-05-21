package com.revy.example.order.command.dto;

import com.revy.example.domain.account.enums.OrderSide;
import com.revy.example.domain.account.enums.OrderType;

import java.math.BigDecimal;
import java.time.Instant;

public record PlaceOrderCommand(
        Long       accountId,
        Long       stockId,
        OrderSide  side,
        OrderType  orderType,
        BigDecimal quantity,
        BigDecimal limitPrice,   // null for MARKET
        String     referenceId,
        Instant    orderedAt
) {}
