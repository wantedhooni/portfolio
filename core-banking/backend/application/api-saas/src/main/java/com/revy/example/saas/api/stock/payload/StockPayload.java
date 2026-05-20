package com.revy.example.saas.api.stock.payload;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

public class StockPayload {

    @Schema(name = "SaasStockPayload.SearchRequest")
    public record SearchRequest(
            String ticker,
            String name,
            String exchange,
            String sector,
            String currency
    ) {}

    @Schema(name = "SaasStockPayload.ModelResponse")
    public record ModelResponse(
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
    ) {}
}
