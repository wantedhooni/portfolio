package com.revy.example.domain.account;


import com.revy.example.domain.account.enums.LotStatus;
import com.revy.example.domain.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "position_lot", indexes = {@Index(name = "idx_lot_position_id", columnList = "position_id"), @Index(name = "idx_lot_position_bought_at", columnList = "position_id,bought_at,id"), @Index(name = "idx_lot_buy_tx_id", columnList = "buy_tx_id"), @Index(name = "idx_lot_status", columnList = "lot_status")})
public class PositionLot extends BaseEntity {

    /**
     * 낙관적 락 버전
     * 동시 매도 요청이 같은 로트의 remainingQuantity를 동시에 차감하면
     * 음수 잔량이 발생할 수 있다.
     *
     * @Version으로 충돌을 감지해 선착순 1건만 성공시키고 나머지는 재시도.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    private StockPosition position;

    /**
     * 매수 원거래 — 취득원가 추적의 기준점
     */
    @Column(name = "buy_tx_id", nullable = false)
    private Long buyTxId;

    /**
     * 최초 매수 수량 (불변)
     */
    @Column(name = "original_quantity", nullable = false, precision = 20, scale = 8)
    private BigDecimal originalQuantity;

    /**
     * 남은 수량 (매도 시 차감)
     */
    @Column(name = "remaining_quantity", nullable = false, precision = 20, scale = 8)
    private BigDecimal remainingQuantity;

    /**
     * 매수 체결 단가 (불변 — 취득원가)
     */
    @Column(name = "buy_price", nullable = false, precision = 20, scale = 4)
    private BigDecimal buyPrice;

    /**
     * 실제 매수 체결 시각 (FIFO 정렬 기준)
     */
    @Column(name = "bought_at", nullable = false)
    private Instant boughtAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "lot_status", nullable = false, length = 20)
    private LotStatus lotStatus;        // OPEN / PARTIAL / CLOSED

    @OneToMany(mappedBy = "lot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LotDisposal> disposals = new ArrayList<>();

    // ── 팩토리 ────────────────────────────────────────────────────
    static PositionLot create(StockPosition position,
                              Long buyTxId,
                              BigDecimal quantity,
                              BigDecimal buyPrice,
                              Instant boughtAt) {
        PositionLot lot = new PositionLot();
        lot.position = position;
        lot.buyTxId = buyTxId;
        lot.originalQuantity = quantity;
        lot.remainingQuantity = quantity;
        lot.buyPrice = buyPrice;
        lot.boughtAt = boughtAt;
        lot.lotStatus = LotStatus.OPEN;
        return lot;
    }

    // ── 비즈니스 ──────────────────────────────────────────────────

    /**
     * 이 로트에서 일부(또는 전량)를 처분하고 LotDisposal을 반환
     */
    LotDisposal dispose(Long sellTxId, BigDecimal qty, BigDecimal sellPrice, Instant disposedAt) {
        if (this.remainingQuantity.compareTo(qty) < 0) throw new IllegalArgumentException("처분 수량이 잔여 수량을 초과합니다.");

        this.remainingQuantity = this.remainingQuantity.subtract(qty);
        this.lotStatus = this.remainingQuantity.compareTo(BigDecimal.ZERO) == 0 ? LotStatus.CLOSED : LotStatus.PARTIAL;

        LotDisposal disposal = LotDisposal.of(this, sellTxId, qty, sellPrice, disposedAt);
        this.disposals.add(disposal);
        return disposal;
    }
}