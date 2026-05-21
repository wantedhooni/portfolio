package com.revy.example.billing.command.dto;

import com.revy.example.domain.billing.enums.BillingItemType;

import java.math.BigDecimal;

public record AddBillingItemCommand(
        Long invoiceId,
        BillingItemType type,
        String description,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal taxRate
) {}
