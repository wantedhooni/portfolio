package com.revy.example.saas.api.trade.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public class TradePayload {

    @Schema(name = "SaasTradePayload.BuyRequest")
    public record BuyRequest(
            @NotNull Long stockId,
            @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal quantity,
            @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal price,
            @NotNull @DecimalMin(value = "0.0") BigDecimal fee,
            @NotNull @DecimalMin(value = "0.0") BigDecimal tax,
            @NotBlank String referenceId,
            @NotNull Instant tradedAt
    ) {}

    @Schema(name = "SaasTradePayload.SellRequest")
    public record SellRequest(
            @NotNull Long stockId,
            @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal quantity,
            @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal price,
            @NotNull @DecimalMin(value = "0.0") BigDecimal fee,
            @NotNull @DecimalMin(value = "0.0") BigDecimal tax,
            @NotBlank String referenceId,
            @NotNull Instant tradedAt
    ) {}

    @Schema(name = "SaasTradePayload.DividendRequest")
    public record DividendRequest(
            @NotNull Long stockId,
            @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal grossAmount,
            @NotNull @DecimalMin(value = "0.0") BigDecimal tax,
            @NotBlank String referenceId,
            @NotNull Instant tradedAt
    ) {}
}
