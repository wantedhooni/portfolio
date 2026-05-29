package com.revy.example.pg.command;

import com.revy.example.pg.command.dto.CreatePgMerchantCommand;
import com.revy.example.pg.command.dto.CreatePgPaymentCommand;
import com.revy.example.pg.command.dto.CreatePgSettlementCommand;

import java.util.List;

public interface PgCommand {

    // ── Merchant ─────────────────────────────────────────────────
    Long createMerchant(CreatePgMerchantCommand command);
    void updateCommissionRate(Long merchantId, java.math.BigDecimal commissionRate);
    void deactivateMerchant(Long merchantId);
    void activateMerchant(Long merchantId);

    // ── Payment ──────────────────────────────────────────────────
    /** 결제 요청 → REQUESTED 상태로 생성, 반환값은 PgPayment ID */
    Long requestPayment(CreatePgPaymentCommand command);

    /** 결제 승인 → APPROVED */
    void approvePayment(Long paymentId);

    /** 결제 취소 → CANCELLED */
    void cancelPayment(Long paymentId);

    // ── Settlement ───────────────────────────────────────────────
    /**
     * PG 정산 생성 (PENDING).
     * @return 생성된 PgSettlement ID
     */
    Long createSettlement(CreatePgSettlementCommand command);

    /**
     * 정산 완료 처리 (PENDING → SETTLED) + 포함 결제의 pgSettlementId 연결.
     */
    void completeSettlement(Long settlementId, List<Long> paymentIds);

    /** 정산 실패 처리 (PENDING → FAILED). */
    void failSettlement(Long settlementId, String reason);
}
