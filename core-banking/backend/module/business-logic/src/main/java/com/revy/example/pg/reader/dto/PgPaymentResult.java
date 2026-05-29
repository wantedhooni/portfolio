package com.revy.example.pg.reader.dto;

import com.revy.example.domain.pg.PgPayment;
import com.revy.example.domain.pg.enums.PaymentMethod;
import com.revy.example.domain.pg.enums.PgPaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PgPaymentResult(
        Long             id,
        Long             merchantId,
        PaymentMethod    paymentMethod,
        String           orderNo,
        BigDecimal       amount,
        BigDecimal       commissionAmount,
        BigDecimal       netAmount,
        String           currency,
        PgPaymentStatus  status,
        Instant          requestedAt,
        Instant          approvedAt,
        Long             pgSettlementId
) {
    public static PgPaymentResult from(PgPayment p) {
        return new PgPaymentResult(
                p.getId(), p.getMerchantId(), p.getPaymentMethod(), p.getOrderNo(),
                p.getAmount(), p.getCommissionAmount(), p.getNetAmount(), p.getCurrency(),
                p.getStatus(), p.getRequestedAt(), p.getApprovedAt(), p.getPgSettlementId()
        );
    }
}
