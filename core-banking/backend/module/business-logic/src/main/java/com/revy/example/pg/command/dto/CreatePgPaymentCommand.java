package com.revy.example.pg.command.dto;

import com.revy.example.domain.pg.enums.PaymentMethod;

import java.math.BigDecimal;

public record CreatePgPaymentCommand(
        Long          merchantId,
        PaymentMethod paymentMethod,
        String        orderNo,
        BigDecimal    amount,
        String        currency
) {}
