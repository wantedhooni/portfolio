package com.revy.example.domain.account;


import com.revy.example.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "lot_disposal",
    indexes = {
        @Index(name = "idx_disposal_lot_id",     columnList = "lot_id"),
        @Index(name = "idx_disposal_sell_tx_id", columnList = "sell_tx_id"),
        @Index(name = "idx_disposal_disposed_at", columnList = "disposed_at")
    }
)
public class LotDisposal extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", nullable = false)
    private PositionLot lot;

    /** 매도 원거래 */
    @Column(name = "sell_tx_id", nullable = false)
    private Long sellTxId;

    @Column(name = "disposed_quantity", nullable = false, precision = 20, scale = 8)
    private BigDecimal disposedQuantity;

    @Column(name = "sell_price", nullable = false, precision = 20, scale = 4)
    private BigDecimal sellPrice;

    /** 실현 손익 = (sell_price - buy_price) × disposed_quantity */
    @Column(name = "realized_pnl", nullable = false, precision = 20, scale = 4)
    private BigDecimal realizedPnl;

    @Column(name = "disposed_at", nullable = false)
    private Instant disposedAt;

    // ── 팩토리 ────────────────────────────────────────────────────
    static LotDisposal of(PositionLot lot, Long sellTxId,
                          BigDecimal qty, BigDecimal sellPrice, Instant disposedAt) {
        LotDisposal d = new LotDisposal();
        d.lot               = lot;
        d.sellTxId          = sellTxId;
        d.disposedQuantity  = qty;
        d.sellPrice         = sellPrice;
        d.realizedPnl       = sellPrice.subtract(lot.getBuyPrice()).multiply(qty);
        d.disposedAt        = disposedAt;
        return d;
    }
}