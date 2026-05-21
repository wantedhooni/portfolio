package com.revy.example.domain.billing;

import com.revy.example.domain.billing.enums.BillingItemType;
import com.revy.example.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** 청구서 항목 — BillingInvoice 애그리거트의 일부. */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "billing_item",
    indexes = @Index(name = "idx_billing_item_invoice_id", columnList = "invoice_id")
)
public class BillingItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private BillingInvoice invoice;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private BillingItemType type;

    @Column(name = "description", nullable = false, length = 200)
    private String description;

    @Column(name = "quantity", nullable = false, precision = 10, scale = 4)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 20, scale = 4)
    private BigDecimal unitPrice;

    /** 소계 = quantity × unitPrice */
    @Column(name = "amount", nullable = false, precision = 20, scale = 4)
    private BigDecimal amount;

    // ── 팩토리 ─────────────────────────────────────────────────

    static BillingItem of(BillingInvoice invoice, BillingItemType type,
                          String description, BigDecimal quantity, BigDecimal unitPrice) {
        BillingItem item = new BillingItem();
        item.invoice     = invoice;
        item.type        = type;
        item.description = description;
        item.quantity    = quantity;
        item.unitPrice   = unitPrice;
        item.amount      = unitPrice.multiply(quantity).setScale(4, RoundingMode.HALF_UP);
        return item;
    }
}
