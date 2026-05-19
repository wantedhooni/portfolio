package com.revy.example.stock.reader.dto;

import com.revy.example.domain.account.Stock;

import java.math.BigDecimal;
import java.time.Instant;

public record StockResult(
        Long id,
        String ticker,
        String name,
        String exchange,
        String sector,
        String currency,
        BigDecimal lastPrice,
        BigDecimal marketCap,
        boolean isActive,
        Instant lastSyncedAt
) {

    public static StockResult from(Stock stock) {
        return new StockResult(
            stock.getId(),
            stock.getTicker(),
            stock.getName(),
            stock.getExchange(),
            stock.getSector(),
            stock.getCurrency(),
            stock.getLastPrice(),
            stock.getMarketCap(),
            stock.isActive(),
            stock.getLastSyncedAt()
        );
    }
}
