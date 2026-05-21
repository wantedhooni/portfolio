package com.revy.example.billing.command.dto;

public record CreateInvoiceCommand(
        Long   accountId,
        String billingPeriod,
        String currency,
        String note
) {}
