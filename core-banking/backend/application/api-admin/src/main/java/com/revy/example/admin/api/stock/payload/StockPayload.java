package com.revy.example.admin.api.stock.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public class StockPayload {

    @Schema(name = "StockPayload.CreateRequest")
    public record CreateRequest(
            @NotBlank String ticker,
            @NotBlank String name,
            @NotBlank String exchange,
            String sector,
            @NotBlank String currency
    ) {}

    @Schema(name = "StockPayload.UpdateRequest")
    public record UpdateRequest() {} // not used — use market-data endpoint

    @Schema(name = "StockPayload.SearchRequest")
    public record SearchRequest(
            String ticker,
            String name,
            String exchange,
            String sector,
            String currency,
            Boolean isActive
    ) {}

    @Schema(name = "StockPayload.ModelResponse")
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

    @Schema(name = "StockPayload.MarketDataRequest")
    public record MarketDataRequest(
            @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal lastPrice,
            BigDecimal marketCap
    ) {}
}
