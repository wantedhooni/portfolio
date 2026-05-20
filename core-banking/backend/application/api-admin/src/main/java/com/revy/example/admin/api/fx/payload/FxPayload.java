package com.revy.example.admin.api.fx.payload;

import com.revy.example.domain.fx.enums.FxStatus;
import com.revy.example.domain.fx.enums.RateType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public class FxPayload {

    // ── Currency ─────────────────────────────────────────────────

    @Schema(name = "FxPayload.RegisterCurrencyRequest")
    public record RegisterCurrencyRequest(
            @NotBlank @Size(min = 3, max = 3) String code,
            @NotBlank String name,
            @NotBlank String symbol,
            @NotNull @Min(0) Integer decimalPlaces
    ) {}

    @Schema(name = "FxPayload.CurrencyResponse")
    public record CurrencyResponse(
            Long id,
            String code,
            String name,
            String symbol,
            Integer decimalPlaces,
            boolean isActive
    ) {}

    // ── ExchangeRate ─────────────────────────────────────────────

    @Schema(name = "FxPayload.QuoteRateRequest")
    public record QuoteRateRequest(
            @NotBlank String baseCurrencyCode,
            @NotBlank String quoteCurrencyCode,
            @NotNull RateType rateType,
            @NotNull @DecimalMin("0.0") BigDecimal rate,
            @NotNull Instant quotedAt,
            @NotBlank String source
    ) {}

    @Schema(name = "FxPayload.RateSearchRequest")
    public record RateSearchRequest(
            String baseCurrencyCode,
            String quoteCurrencyCode,
            RateType rateType,
            String source,
            Instant quotedFrom,
            Instant quotedTo
    ) {}

    @Schema(name = "FxPayload.ExchangeRateResponse")
    public record ExchangeRateResponse(
            Long id,
            String baseCurrencyCode,
            String quoteCurrencyCode,
            RateType rateType,
            BigDecimal rate,
            Instant quotedAt,
            String source
    ) {}

    // ── FxConversion ─────────────────────────────────────────────

    @Schema(name = "FxPayload.ConvertRequest")
    public record ConvertRequest(
            @NotNull Long fromAccountId,
            @NotNull Long toAccountId,
            @NotBlank String fromCurrencyCode,
            @NotBlank String toCurrencyCode,
            @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal fromAmount,
            @NotNull RateType rateType,
            @NotNull @DecimalMin("0.0") BigDecimal fee,
            @NotBlank String referenceId
    ) {}

    @Schema(name = "FxPayload.ConversionResponse")
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
            Long debitTxId,
            Long creditTxId,
            Instant executedAt,
            String referenceId
    ) {}
}
