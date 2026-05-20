package com.revy.example.domain.insurance;

import com.revy.example.domain.common.BaseEntity;
import com.revy.example.domain.insurance.enums.PaymentStatus;
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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "premium_payment",
    uniqueConstraints = @UniqueConstraint(name = "uq_premium_reference", columnNames = "reference_id"),
    indexes = {
        @Index(name = "idx_premium_policy_id",    columnList = "policy_id"),
        @Index(name = "idx_premium_due_date",     columnList = "due_date"),
        @Index(name = "idx_premium_status",       columnList = "status"),
        @Index(name = "idx_premium_account_tx",   columnList = "account_tx_id")
    }
)
public class PremiumPayment extends BaseEntity {

    @Column(name = "policy_id", nullable = false)
    private Long policyId;

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "paid_at")
    private Instant paidAt;

    /** 출금 계좌 — 정기 자동이체 */
    @Column(name = "billing_account_id", nullable = false)
    private Long billingAccountId;

    /** 출금이 발생한 AccountTx FK (감사 추적용) */
    @Column(name = "account_tx_id")
    private Long accountTxId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    /** 멱등성 키 — 자동이체 재시도 중복 방지 */
    @Column(name = "reference_id", nullable = false, length = 64)
    private String referenceId;

    @Column(name = "failure_reason", length = 200)
    private String failureReason;

    // ── 팩토리 ────────────────────────────────────────────────────
    public static PremiumPayment schedule(Long policyId, BigDecimal amount, String currency,
                                          LocalDate dueDate, Long billingAccountId, String referenceId) {
        PremiumPayment p = new PremiumPayment();
        p.policyId           = policyId;
        p.amount             = amount;
        p.currency           = currency;
        p.dueDate            = dueDate;
        p.billingAccountId   = billingAccountId;
        p.referenceId        = referenceId;
        p.status             = PaymentStatus.PENDING;
        return p;
    }

    // ── 비즈니스 ──────────────────────────────────────────────────
    public void markPaid(Long accountTxId, Instant paidAt) {
        this.accountTxId = accountTxId;
        this.paidAt      = paidAt;
        this.status      = PaymentStatus.PAID;
    }

    public void markOverdue() {
        if (this.status == PaymentStatus.PENDING) {
            this.status = PaymentStatus.OVERDUE;
        }
    }

    public void markFailed(String reason) {
        this.status         = PaymentStatus.FAILED;
        this.failureReason  = reason;
    }

    public void waive() {
        this.status = PaymentStatus.WAIVED;
    }
}
