package com.revy.example.order.reader.dto;

import com.revy.example.domain.account.StockOrder;
import com.revy.example.domain.account.enums.OrderSide;
import com.revy.example.domain.account.enums.OrderStatus;
import com.revy.example.domain.account.enums.OrderType;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResult(
        Long        id,
        Long        accountId,
        Long        stockId,
        OrderSide   side,
        OrderType   orderType,
        BigDecimal  quantity,
        BigDecimal  limitPrice,
        BigDecimal  filledQuantity,
        BigDecimal  remainingQuantity,
        BigDecimal  avgFillPrice,
        OrderStatus status,
        String      referenceId,
        Instant     orderedAt,
        Instant     filledAt,
        Instant     cancelledAt
) {
    public static OrderResult from(StockOrder o) {
        return new OrderResult(
            o.getId(), o.getAccountId(), o.getStockId(),
            o.getSide(), o.getOrderType(),
            o.getQuantity(), o.getLimitPrice(),
            o.getFilledQuantity(), o.remainingQuantity(),
            o.getAvgFillPrice(), o.getStatus(),
            o.getReferenceId(), o.getOrderedAt(),
            o.getFilledAt(), o.getCancelledAt()
        );
    }
}
