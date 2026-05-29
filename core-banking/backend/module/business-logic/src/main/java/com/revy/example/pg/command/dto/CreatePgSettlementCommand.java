package com.revy.example.pg.command.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * PG 정산 생성 커맨드.
 *
 * <p>{@code paymentIds}는 이번 정산에 포함되는 PgPayment ID 목록으로,
 * 정산 생성 후 각 결제의 {@code pgSettlementId}를 업데이트하는 데 사용된다.
 */
public record CreatePgSettlementCommand(
        Long          merchantId,
        LocalDate     targetDate,       // 매출 기준일
        LocalDate     settlementDate,   // 정산 지급일
        String        currency,
        int           paymentCount,
        BigDecimal    totalAmount,
        BigDecimal    commissionAmount,
        BigDecimal    netAmount,
        String        referenceId,      // PGSTL-{merchantId}-{targetDate}
        List<Long>    paymentIds        // 연결할 결제 ID 목록
) {}
