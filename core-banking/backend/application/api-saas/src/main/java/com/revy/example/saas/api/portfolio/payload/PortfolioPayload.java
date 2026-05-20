package com.revy.example.saas.api.portfolio.payload;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

public class PortfolioPayload {

    @Schema(name = "SaasPortfolioPayload.PositionResponse")
    public record PositionResponse(
            Long stockId,
            String ticker,
            String stockName,
            BigDecimal totalQuantity,
            BigDecimal averageBuyPrice,
            BigDecimal currentPrice,
            BigDecimal realizedPnl,
            BigDecimal unrealizedPnl
    ) {}

    @Schema(name = "SaasPortfolioPayload.ModelResponse")
    public record ModelResponse(
            Long accountId,
            BigDecimal balance,
            BigDecimal availableBalance,
            BigDecimal totalRealizedPnl,
            BigDecimal totalUnrealizedPnl,
            List<PositionResponse> positions
    ) {}
}
