package com.revy.example.saas.api.fx.payload;

import com.revy.example.domain.fx.enums.FxStatus;
import com.revy.example.domain.fx.enums.RateType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public class FxPayload {

    // ── Currency ─────────────────────────────────────────────────

    @Schema(name = "SaasFxPayload.CurrencyResponse")
    public record CurrencyResponse(
            String code,
            String name,
            String symbol,
            Integer decimalPlaces
    ) {}

    // ── ExchangeRate ─────────────────────────────────────────────

    @Schema(name = "SaasFxPayload.RateResponse")
    public record RateResponse(
            String baseCurrencyCode,
            String quoteCurrencyCode,
            RateType rateType,
            BigDecimal rate,
            Instant quotedAt,
            String source
    ) {}

    // ── Conversion ───────────────────────────────────────────────

    @Schema(name = "SaasFxPayload.ConvertRequest")
    public record ConvertRequest(
            @NotNull Long fromAccountId,
            @NotNull Long toAccountId,
            @NotBlank @Size(min = 3, max = 3) String fromCurrencyCode,
            @NotBlank @Size(min = 3, max = 3) String toCurrencyCode,
            @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal fromAmount,
            @NotNull RateType rateType,
            @DecimalMin(value = "0.0") BigDecimal fee,
            @NotBlank String referenceId
    ) {}

    @Schema(name = "SaasFxPayload.ConversionResponse")
    public record ConversionResponse(
            Long id,
            String conversionNumber,
            Long fromAccountId,
            Long toAccountId,
            String fromCurrencyCode,
            String toCurrencyCode,
            BigDecimal fromAmount,
            BigDecimal toAmount,
            BigDecimal appliedRate,
            RateType appliedRateType,
            BigDecimal fee,
            FxStatus status,
            Instant executedAt,
            String referenceId
    ) {}
}
