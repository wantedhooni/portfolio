package com.revy.example.billing.reader.dto;

import com.revy.example.domain.billing.BillingInvoice;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record BillingInvoiceResult(
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
        List<BillingItemResult> items,
        Instant createdAt
) {
    public static BillingInvoiceResult from(BillingInvoice inv) {
        List<BillingItemResult> items = inv.getItems().stream()
                .map(BillingItemResult::from)
                .toList();
        return new BillingInvoiceResult(
                inv.getId(), inv.getAccountId(), inv.getBillingPeriod(),
                inv.getStatus().name(), inv.getCurrency(),
                inv.getSubtotal(), inv.getTaxAmount(), inv.getTotalAmount(),
                inv.getDueDate(), inv.getIssuedAt(), inv.getPaidAt(),
                inv.getNote(), items, inv.getCreatedAt()
        );
    }
}
