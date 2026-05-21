package com.revy.example.admin.api.settlement.payload;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;

public class SettlementPayload {

    public record CreateRequest(
            @NotNull Long accountId,
            @NotBlank String type,
            @NotNull LocalDate settlementDate,
            @NotBlank String currency,
            @NotNull @DecimalMin("0") BigDecimal grossAmount,
            @NotNull @DecimalMin("0") BigDecimal feeAmount,
            @NotNull @DecimalMin("0") BigDecimal taxAmount,
            @NotNull BigDecimal netAmount,
            String referenceId,
            String note
    ) {}

    public record FailRequest(@NotBlank String reason) {}

    public record SearchRequest(
            Long accountId,
            String type,
            String status,
            LocalDate settlementDateFrom,
            LocalDate settlementDateTo
    ) {}

    public record ModelResponse(
            Long id,
            Long accountId,
            String type,
            LocalDate settlementDate,
            String status,
            String currency,
            BigDecimal grossAmount,
            BigDecimal feeAmount,
            BigDecimal taxAmount,
            BigDecimal netAmount,
            String referenceId,
            String note,
            String failedReason,
            Instant settledAt,
            Instant createdAt
    ) {}
}
