package com.revy.example.billing.reader.dto;

import com.revy.example.domain.billing.enums.InvoiceStatus;

public record BillingSearchCondition(
        Long accountId,
        String billingPeriod,
        InvoiceStatus status
) {}
