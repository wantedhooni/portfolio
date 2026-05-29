package com.revy.example.domain.pg;

import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.domain.common.BaseEntity;
import com.revy.example.domain.pg.enums.PgSettlementStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * PG 가맹점별 일별 정산.
 *
 * <p>정산 배치가 승인 완료된 결제를 가맹점별·일별로 집계해 생성한다.
 * 정산 주기(T+n)에 따라 {@code targetDate}의 결제가 {@code settlementDate}에 지급된다.
 *
 * <p>상태 흐름: PENDING → SETTLED | FAILED
 *
 * <h3>원장 분개 (Step 2)</h3>
 * <pre>
 * DR 현금          (1001) = totalAmount
 *    CR 가맹점정산부채 (2002) = netAmount
 *    CR 수수료수입    (4002) = commissionAmount
 * </pre>
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "pg_settlement",
    uniqueConstraints = @UniqueConstraint(name = "uq_pg_settlement_ref", columnNames = "reference_id"),
    indexes = {
        @Index(name = "idx_pg_stl_merchant",         columnList = "merchant_id"),
        @Index(name = "idx_pg_stl_settlement_date",  columnList = "settlement_date"),
        @Index(name = "idx_pg_stl_target_date",      columnList = "target_date"),
        @Index(name = "idx_pg_stl_status",           columnList = "status")
    }
)
public class PgSettlement extends BaseEntity {

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    /** 매출 기준일 (PgPayment.approvedAt 기준 날짜) */
    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    /** 실제 가맹점 계좌 입금일 = targetDate + settlementCycle */
    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate;

    /** 정산 포함 결제 건수 */
    @Column(name = "payment_count", nullable = false)
    private int paymentCount;

    /** 총 결제금액 */
    @Column(name = "total_amount", nullable = false, precision = 20, scale = 2)
    private BigDecimal totalAmount;

    /** 총 수수료 */
    @Column(name = "commission_amount", nullable = false, precision = 20, scale = 2)
    private BigDecimal commissionAmount;

    /** 가맹점 지급액 = totalAmount − commissionAmount */
    @Column(name = "net_amount", nullable = false, precision = 20, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PgSettlementStatus status;

    @Column(name = "settled_at")
    private Instant settledAt;

    @Column(name = "failed_reason", length = 500)
    private String failedReason;

    /** 멱등성 키 = "PGSTL-{merchantId}-{targetDate}" */
    @Column(name = "reference_id", nullable = false, length = 64)
    private String referenceId;

    // ── 팩토리 ────────────────────────────────────────────────────

    public static PgSettlement create(
            Long merchantId, LocalDate targetDate, LocalDate settlementDate,
            int paymentCount, BigDecimal totalAmount, BigDecimal commissionAmount,
            BigDecimal netAmount, String currency, String referenceId) {
        PgSettlement s = new PgSettlement();
        s.merchantId        = merchantId;
        s.targetDate        = targetDate;
        s.settlementDate    = settlementDate;
        s.paymentCount      = paymentCount;
        s.totalAmount       = totalAmount;
        s.commissionAmount  = commissionAmount;
        s.netAmount         = netAmount;
        s.currency          = currency;
        s.status            = PgSettlementStatus.PENDING;
        s.referenceId       = referenceId;
        return s;
    }

    // ── 도메인 행위 ─────────────────────────────────────────────

    public void complete() {
        if (this.status != PgSettlementStatus.PENDING) {
            throw new BusinessException(ErrorCode.PG_SETTLEMENT_NOT_PENDING);
        }
        this.status    = PgSettlementStatus.SETTLED;
        this.settledAt = Instant.now();
    }

    public void fail(String reason) {
        if (this.status != PgSettlementStatus.PENDING) {
            throw new BusinessException(ErrorCode.PG_SETTLEMENT_NOT_PENDING);
        }
        this.status       = PgSettlementStatus.FAILED;
        this.failedReason = reason;
    }
}
