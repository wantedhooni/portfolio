package com.revy.example.admin.api.billing.payload;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class BillingPayload {

    public record CreateInvoiceRequest(
            @NotNull Long accountId,
            @NotBlank String billingPeriod,
            @NotBlank String currency,
            String note
    ) {}

    public record AddItemRequest(
            @NotBlank String type,
            @NotBlank String description,
            @NotNull @DecimalMin("0") BigDecimal quantity,
            @NotNull @DecimalMin("0") BigDecimal unitPrice,
            @NotNull @DecimalMin("0") BigDecimal taxRate
    ) {}

    public record IssueRequest(@NotNull LocalDate dueDate) {}

    public record SearchRequest(
            Long accountId,
            String billingPeriod,
            String status
    ) {}

    public record ItemResponse(
            Long id,
            String type,
            String description,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal amount
    ) {}

    public record ModelResponse(
            Long id,
            Long accountId,
            String billingPeriod,
            String status,
            String currency,
            BigDecimal subtotal,
            BigDecimal taxAmount,
            BigDecimal totalAmount,
            LocalDate dueDate,
            Instant issuedAt,
            Instant paidAt,
            String note,
            List<ItemResponse> items,
            Instant createdAt
    ) {}
}
