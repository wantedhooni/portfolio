package com.revy.example.portfolio.reader.dto;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioResult(
        Long accountId,
        BigDecimal balance,
        BigDecimal availableBalance,
        BigDecimal totalRealizedPnl,
        BigDecimal totalUnrealizedPnl,
        List<PortfolioPositionResult> positions
) {}
