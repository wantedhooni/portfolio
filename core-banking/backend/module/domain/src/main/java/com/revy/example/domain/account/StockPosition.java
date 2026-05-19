package com.revy.example.domain.account;

import com.revy.example.domain.account.enums.LotStatus;
import com.revy.example.domain.account.exception.InsufficientPositionException;
import com.revy.example.domain.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "stock_position", uniqueConstraints = @UniqueConstraint(name = "uq_position_account_stock", columnNames = {"account_id", "stock_id"}), indexes = {@Index(name = "idx_position_account_id", columnList = "account_id"), @Index(name = "idx_position_stock_id", columnList = "stock_id")})
public class StockPosition extends BaseEntity {

    /**
     * 낙관적 락 버전
     * 동일 계좌·종목에 대해 동시 매수/매도가 들어오면
     * totalQuantity·realizedPnl이 동시에 갱신될 수 있다.
     * 충돌 감지 후 서비스 레이어에서 재시도.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "stock_id", nullable = false)
    private Long stockId;

    /**
     * 전체 보유 수량 = 로트들의 remaining_quantity 합산 (정합성 캐시)
     */
    @Column(name = "total_quantity", nullable = false, precision = 20, scale = 8)
    private BigDecimal totalQuantity;

    /**
     * 누적 실현 손익 = LotDisposal.realized_pnl 합산 (정합성 캐시)
     */
    @Column(name = "realized_pnl", nullable = false, precision = 20, scale = 4)
    private BigDecimal realizedPnl;

    @OneToMany(mappedBy = "position", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("bought_at ASC, id ASC")  // FIFO 정렬 보장
    private List<PositionLot> lots = new ArrayList<>();

    // ── 팩토리 ────────────────────────────────────────────────────
    public static StockPosition create(Long accountId, Long stockId) {
        StockPosition p = new StockPosition();
        p.accountId = accountId;
        p.stockId = stockId;
        p.totalQuantity = BigDecimal.ZERO;
        p.realizedPnl = BigDecimal.ZERO;
        return p;
    }

    // ── 비즈니스 ──────────────────────────────────────────────────

    /**
     * 매수: 새 로트 추가
     */
    public PositionLot addLot(Long buyTxId, BigDecimal quantity, BigDecimal buyPrice, Instant boughtAt) {
        PositionLot lot = PositionLot.create(this, buyTxId, quantity, buyPrice, boughtAt);
        this.lots.add(lot);
        this.totalQuantity = this.totalQuantity.add(quantity);
        return lot;
    }

    /**
     * 매도: FIFO 순서로 로트 소진 후 실현 손익 반환
     *
     * @param sellQty   매도 수량
     * @param sellPrice 매도 단가
     * @param sellTxId  매도 AccountTx ID
     * @return 생성된 LotDisposal 목록 (하나의 매도가 여러 로트에 걸칠 수 있음)
     */
    public List<LotDisposal> sell(Long sellTxId, BigDecimal sellQty, BigDecimal sellPrice, Instant soldAt) {
        if (this.totalQuantity.compareTo(sellQty) < 0) throw new InsufficientPositionException();

        List<LotDisposal> disposals = new ArrayList<>();
        BigDecimal remaining = sellQty;

        for (PositionLot lot : activeLots()) {
            if (remaining.compareTo(BigDecimal.ZERO) == 0) break;

            BigDecimal consume = remaining.min(lot.getRemainingQuantity());
            LotDisposal disposal = lot.dispose(sellTxId, consume, sellPrice, soldAt);
            disposals.add(disposal);

            this.realizedPnl = this.realizedPnl.add(disposal.getRealizedPnl());
            remaining = remaining.subtract(consume);
        }

        this.totalQuantity = this.totalQuantity.subtract(sellQty);
        return disposals;
    }

    /**
     * 미실현 손익 (현재가 기준, 외부 주입)
     */
    public BigDecimal unrealizedPnl(BigDecimal currentPrice) {
        return activeLots().stream()
                           .map(lot -> currentPrice.subtract(lot.getBuyPrice()).multiply(lot.getRemainingQuantity()))
                           .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isClosed() {
        return this.totalQuantity.compareTo(BigDecimal.ZERO) == 0;
    }

    private List<PositionLot> activeLots() {
        return lots.stream()
                   .filter(lot -> lot.getLotStatus() == LotStatus.OPEN || lot.getLotStatus() == LotStatus.PARTIAL)
                   .toList();

    }
}