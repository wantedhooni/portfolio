package com.revy.example.saas.api.billing.payload;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class BillingPayload {

    public record SearchRequest(
            Long accountId,        // 선택. 미지정 시 본인 소유 전체 계좌
            String billingPeriod,  // 선택. "YYYY-MM" prefix LIKE 검색
            String status          // 선택. DRAFT/ISSUED/PAID/OVERDUE/CANCELLED
    ) {}

    public record ItemResponse(
            Long id,
            String type,
            String description,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal amount
    ) {}

    public record InvoiceResponse(
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
