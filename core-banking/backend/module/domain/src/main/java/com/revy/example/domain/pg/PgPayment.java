package com.revy.example.domain.pg;

import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.domain.common.BaseEntity;
import com.revy.example.domain.pg.enums.PaymentMethod;
import com.revy.example.domain.pg.enums.PgPaymentStatus;
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

/**
 * PG 결제.
 *
 * <p>상태 흐름: REQUESTED → APPROVED | FAILED<br>
 * APPROVED → CANCELLED | REFUNDED
 *
 * <p>정산 처리 후 {@code pgSettlementId}가 설정된다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "pg_payment",
    uniqueConstraints = @UniqueConstraint(name = "uq_pg_payment_order_no", columnNames = "order_no"),
    indexes = {
        @Index(name = "idx_pg_payment_merchant_id",   columnList = "merchant_id"),
        @Index(name = "idx_pg_payment_status",        columnList = "status"),
        @Index(name = "idx_pg_payment_approved_at",   columnList = "approved_at"),
        @Index(name = "idx_pg_payment_settlement_id", columnList = "pg_settlement_id"),
        @Index(name = "idx_pg_payment_method",        columnList = "payment_method")
    }
)
public class PgPayment extends BaseEntity {

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    /** 주문번호 — 가맹점이 발행하는 고유 식별자 (멱등성 키) */
    @Column(name = "order_no", nullable = false, length = 64)
    private String orderNo;

    /** 결제 금액 */
    @Column(name = "amount", nullable = false, precision = 20, scale = 2)
    private BigDecimal amount;

    /** PG 수수료 = amount × merchant.commissionRate */
    @Column(name = "commission_amount", nullable = false, precision = 20, scale = 2)
    private BigDecimal commissionAmount;

    /** 가맹점 정산 예정액 = amount − commissionAmount */
    @Column(name = "net_amount", nullable = false, precision = 20, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PgPaymentStatus status;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    /** 연결된 PG 정산 ID (정산 배치 처리 후 설정) */
    @Column(name = "pg_settlement_id")
    private Long pgSettlementId;

    // ── 팩토리 ────────────────────────────────────────────────────

    public static PgPayment request(
            Long merchantId, PaymentMethod method, String orderNo,
            BigDecimal amount, BigDecimal commissionAmount,
            BigDecimal netAmount, String currency) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        PgPayment p = new PgPayment();
        p.merchantId        = merchantId;
        p.paymentMethod     = method;
        p.orderNo           = orderNo;
        p.amount            = amount;
        p.commissionAmount  = commissionAmount;
        p.netAmount         = netAmount;
        p.currency          = currency;
        p.status            = PgPaymentStatus.REQUESTED;
        p.requestedAt       = Instant.now();
        return p;
    }

    // ── 도메인 행위 ─────────────────────────────────────────────

    public void approve() {
        if (this.status != PgPaymentStatus.REQUESTED) {
            throw new BusinessException(ErrorCode.PG_PAYMENT_NOT_APPROVED);
        }
        this.status     = PgPaymentStatus.APPROVED;
        this.approvedAt = Instant.now();
    }

    public void cancel() {
        if (this.status != PgPaymentStatus.APPROVED) {
            throw new BusinessException(ErrorCode.PG_PAYMENT_NOT_APPROVED);
        }
        this.status       = PgPaymentStatus.CANCELLED;
        this.cancelledAt  = Instant.now();
    }

    public void refund() {
        if (this.status != PgPaymentStatus.APPROVED) {
            throw new BusinessException(ErrorCode.PG_PAYMENT_NOT_APPROVED);
        }
        this.status = PgPaymentStatus.REFUNDED;
    }

    public void fail() {
        this.status = PgPaymentStatus.FAILED;
    }

    /** 정산 배치에서 정산 레코드와 연결한다. */
    public void linkToSettlement(Long settlementId) {
        if (this.pgSettlementId != null) {
            throw new BusinessException(ErrorCode.PG_PAYMENT_ALREADY_SETTLED);
        }
        this.pgSettlementId = settlementId;
    }
}
