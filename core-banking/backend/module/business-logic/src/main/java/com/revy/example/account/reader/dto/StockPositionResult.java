package com.revy.example.account.reader.dto;

import com.revy.example.domain.account.StockPosition;

import java.math.BigDecimal;
import java.util.List;

public record StockPositionResult(
        Long id,
        Long accountId,
        Long stockId,
        BigDecimal totalQuantity,
        BigDecimal realizedPnl,
        List<PositionLotResult> lots
) {

    public static StockPositionResult from(StockPosition position) {
        return new StockPositionResult(
            position.getId(),
            position.getAccountId(),
            position.getStockId(),
            position.getTotalQuantity(),
            position.getRealizedPnl(),
            position.getLots().stream().map(PositionLotResult::from).toList()
        );
    }
}
