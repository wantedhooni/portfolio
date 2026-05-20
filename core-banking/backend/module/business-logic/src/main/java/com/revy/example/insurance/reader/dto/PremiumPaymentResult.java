package com.revy.example.insurance.reader.dto;

import com.revy.example.domain.insurance.PremiumPayment;
import com.revy.example.domain.insurance.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record PremiumPaymentResult(
        Long id,
        Long policyId,
        BigDecimal amount,
        String currency,
        LocalDate dueDate,
        Instant paidAt,
        Long billingAccountId,
        Long accountTxId,
        PaymentStatus status,
        String referenceId,
        String failureReason
) {
    public static PremiumPaymentResult from(PremiumPayment p) {
        return new PremiumPaymentResult(
            p.getId(), p.getPolicyId(), p.getAmount(), p.getCurrency(),
            p.getDueDate(), p.getPaidAt(), p.getBillingAccountId(), p.getAccountTxId(),
            p.getStatus(), p.getReferenceId(), p.getFailureReason()
        );
    }
}
