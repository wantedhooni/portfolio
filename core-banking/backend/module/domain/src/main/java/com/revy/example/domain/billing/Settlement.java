package com.revy.example.domain.billing;

import com.revy.example.domain.billing.enums.SettlementStatus;
import com.revy.example.domain.billing.enums.SettlementType;
import com.revy.example.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * 정산 (Settlement) — 특정 거래 또는 기간에 대한 금액 정산 레코드.
 *
 * <p>상태 흐름: PENDING → SETTLED | FAILED | CANCELLED
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "settlement",
    indexes = {
        @Index(name = "idx_settlement_account_id",      columnList = "account_id"),
        @Index(name = "idx_settlement_status",          columnList = "status"),
        @Index(name = "idx_settlement_settlement_date", columnList = "settlement_date"),
        @Index(name = "idx_settlement_reference_id",    columnList = "reference_id"),
    }
)
public class Settlement extends BaseEntity {

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private SettlementType type;

    /** 정산 기준일 */
    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SettlementStatus status;

    /** 정산 통화 */
    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    /** 총 체결금액 (수수료·세금 차감 전) */
    @Column(name = "gross_amount", nullable = false, precision = 20, scale = 4)
    private BigDecimal grossAmount;

    /** 수수료 합계 */
    @Column(name = "fee_amount", nullable = false, precision = 20, scale = 4)
    private BigDecimal feeAmount;

    /** 세금 합계 */
    @Column(name = "tax_amount", nullable = false, precision = 20, scale = 4)
    private BigDecimal taxAmount;

    /** 최종 정산금액 = grossAmount ∓ feeAmount ∓ taxAmount */
    @Column(name = "net_amount", nullable = false, precision = 20, scale = 4)
    private BigDecimal netAmount;

    /** 연결 거래 참조 ID (AccountTx.referenceId 등) */
    @Column(name = "reference_id", length = 64)
    private String referenceId;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "settled_at")
    private Instant settledAt;

    @Column(name = "failed_reason", length = 255)
    private String failedReason;

    // ── 팩토리 ──────────────────────────────────────────────────

    public static Settlement create(Long accountId, SettlementType type,
                                    LocalDate settlementDate, String currency,
                                    BigDecimal grossAmount, BigDecimal feeAmount,
                                    BigDecimal taxAmount, BigDecimal netAmount,
                                    String referenceId, String note) {
        Settlement s = new Settlement();
        s.accountId      = accountId;
        s.type           = type;
        s.settlementDate = settlementDate;
        s.status         = SettlementStatus.PENDING;
        s.currency       = currency;
        s.grossAmount    = grossAmount;
        s.feeAmount      = feeAmount;
        s.taxAmount      = taxAmount;
        s.netAmount      = netAmount;
        s.referenceId    = referenceId;
        s.note           = note;
        return s;
    }

    // ── 도메인 행위 ─────────────────────────────────────────────

    public void settle() {
        if (this.status != SettlementStatus.PENDING) {
            throw new IllegalStateException("PENDING 상태의 정산만 처리할 수 있습니다. 현재 상태: " + this.status);
        }
        this.status    = SettlementStatus.SETTLED;
        this.settledAt = Instant.now();
    }

    public void fail(String reason) {
        if (this.status != SettlementStatus.PENDING) {
            throw new IllegalStateException("PENDING 상태의 정산만 실패 처리할 수 있습니다. 현재 상태: " + this.status);
        }
        this.status       = SettlementStatus.FAILED;
        this.failedReason = reason;
    }

    public void cancel() {
        if (this.status != SettlementStatus.PENDING) {
            throw new IllegalStateException("PENDING 상태의 정산만 취소할 수 있습니다. 현재 상태: " + this.status);
        }
        this.status = SettlementStatus.CANCELLED;
    }
}
