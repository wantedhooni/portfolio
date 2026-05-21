package com.revy.example.domain.billing;

import com.revy.example.domain.billing.enums.BillingItemType;
import com.revy.example.domain.billing.enums.InvoiceStatus;
import com.revy.example.domain.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 청구서 (BillingInvoice) — 계정 단위 월별 수수료 청구 집계.
 *
 * <p>상태 흐름: DRAFT → ISSUED → PAID | OVERDUE | CANCELLED
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "billing_invoice",
    indexes = {
        @Index(name = "idx_billing_invoice_account_id",     columnList = "account_id"),
        @Index(name = "idx_billing_invoice_billing_period", columnList = "billing_period"),
        @Index(name = "idx_billing_invoice_status",         columnList = "status"),
    }
)
public class BillingInvoice extends BaseEntity {

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    /** 청구 기간 (YYYY-MM) */
    @Column(name = "billing_period", nullable = false, length = 7)
    private String billingPeriod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InvoiceStatus status;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    /** 소계 (세금 제외) */
    @Column(name = "subtotal", nullable = false, precision = 20, scale = 4)
    private BigDecimal subtotal;

    /** 세금 합계 */
    @Column(name = "tax_amount", nullable = false, precision = 20, scale = 4)
    private BigDecimal taxAmount;

    /** 청구 총액 = subtotal + taxAmount */
    @Column(name = "total_amount", nullable = false, precision = 20, scale = 4)
    private BigDecimal totalAmount;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "issued_at")
    private Instant issuedAt;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "note", length = 500)
    private String note;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BillingItem> items = new ArrayList<>();

    // ── 팩토리 ──────────────────────────────────────────────────

    public static BillingInvoice create(Long accountId, String billingPeriod,
                                        String currency, String note) {
        BillingInvoice inv = new BillingInvoice();
        inv.accountId     = accountId;
        inv.billingPeriod = billingPeriod;
        inv.status        = InvoiceStatus.DRAFT;
        inv.currency      = currency;
        inv.subtotal      = BigDecimal.ZERO;
        inv.taxAmount     = BigDecimal.ZERO;
        inv.totalAmount   = BigDecimal.ZERO;
        inv.note          = note;
        return inv;
    }

    // ── 도메인 행위 ─────────────────────────────────────────────

    /**
     * 항목 추가 후 소계/합계 재계산.
     */
    public BillingItem addItem(BillingItemType type, String description,
                               BigDecimal quantity, BigDecimal unitPrice, BigDecimal taxRate) {
        if (this.status != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("DRAFT 상태에서만 항목을 추가할 수 있습니다.");
        }
        BillingItem item = BillingItem.of(this, type, description, quantity, unitPrice);
        this.items.add(item);
        recalculate(taxRate);
        return item;
    }

    /** 청구서 발행 — DRAFT → ISSUED */
    public void issue(LocalDate dueDate) {
        if (this.status != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("DRAFT 상태에서만 발행할 수 있습니다. 현재 상태: " + this.status);
        }
        if (this.items.isEmpty()) {
            throw new IllegalStateException("항목이 없는 청구서는 발행할 수 없습니다.");
        }
        this.status   = InvoiceStatus.ISSUED;
        this.dueDate  = dueDate;
        this.issuedAt = Instant.now();
    }

    /** 납부 처리 — ISSUED / OVERDUE → PAID */
    public void markPaid(Instant paidAt) {
        if (this.status != InvoiceStatus.ISSUED && this.status != InvoiceStatus.OVERDUE) {
            throw new IllegalStateException("ISSUED 또는 OVERDUE 상태에서만 납부 처리할 수 있습니다.");
        }
        this.status = InvoiceStatus.PAID;
        this.paidAt = paidAt;
    }

    /** 기한 초과 처리 — ISSUED → OVERDUE */
    public void markOverdue() {
        if (this.status != InvoiceStatus.ISSUED) {
            throw new IllegalStateException("ISSUED 상태에서만 기한 초과 처리할 수 있습니다.");
        }
        this.status = InvoiceStatus.OVERDUE;
    }

    /** 취소 — DRAFT / ISSUED → CANCELLED */
    public void cancel() {
        if (this.status == InvoiceStatus.PAID) {
            throw new IllegalStateException("이미 납부된 청구서는 취소할 수 없습니다.");
        }
        if (this.status == InvoiceStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 청구서입니다.");
        }
        this.status = InvoiceStatus.CANCELLED;
    }

    // ── private ─────────────────────────────────────────────────

    private void recalculate(BigDecimal taxRate) {
        BigDecimal sub = items.stream()
                .map(BillingItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);
        BigDecimal tax = sub.multiply(taxRate).setScale(4, RoundingMode.HALF_UP);
        this.subtotal    = sub;
        this.taxAmount   = tax;
        this.totalAmount = sub.add(tax);
    }
}
