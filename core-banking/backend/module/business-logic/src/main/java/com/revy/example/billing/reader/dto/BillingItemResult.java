package com.revy.example.billing.reader.dto;

import com.revy.example.domain.billing.BillingItem;

import java.math.BigDecimal;

public record BillingItemResult(
        Long id,
        String type,
        String description,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal amount
) {
    public static BillingItemResult from(BillingItem item) {
        return new BillingItemResult(
                item.getId(), item.getType().name(),
                item.getDescription(), item.getQuantity(),
                item.getUnitPrice(), item.getAmount()
        );
    }
}
