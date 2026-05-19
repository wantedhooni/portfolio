package com.revy.example.portfolio.reader.dto;

import java.math.BigDecimal;

public record PortfolioPositionResult(
        Long stockId,
        String ticker,
        String stockName,
        BigDecimal totalQuantity,
        BigDecimal averageBuyPrice,
        BigDecimal currentPrice,
        BigDecimal realizedPnl,
        BigDecimal unrealizedPnl
) {}
