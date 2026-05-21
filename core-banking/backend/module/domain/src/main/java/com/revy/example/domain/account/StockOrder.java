package com.revy.example.domain.account;

import com.revy.example.domain.account.enums.OrderSide;
import com.revy.example.domain.account.enums.OrderStatus;
import com.revy.example.domain.account.enums.OrderType;
import com.revy.example.domain.account.exception.OrderAlreadyExecutedException;
import com.revy.example.domain.account.exception.OrderNotPendingException;
import com.revy.example.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

/**
 * 주식 주문 — PENDING → FILLED | CANCELLED
 *
 * <p>주문의 referenceId 는 체결 시 AccountTx.referenceId 로 재사용됩니다.
 * 따라서 한 주문에 하나의 체결만 생성됩니다 (전량 체결 모델).
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "stock_order",
    uniqueConstraints = @UniqueConstraint(name = "uq_stock_order_reference_id", columnNames = "reference_id"),
    indexes = {
        @Index(name = "idx_stock_order_account_id",  columnList = "account_id"),
        @Index(name = "idx_stock_order_stock_id",    columnList = "stock_id"),
        @Index(name = "idx_stock_order_status",      columnList = "status"),
        @Index(name = "idx_stock_order_ordered_at",  columnList = "ordered_at")
    }
)
public class StockOrder extends BaseEntity {

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "stock_id", nullable = false)
    private Long stockId;

    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false, length = 10)
    private OrderSide side;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 10)
    private OrderType orderType;

    /** 주문 수량 */
    @Column(name = "quantity", nullable = false, precision = 20, scale = 8)
    private BigDecimal quantity;

    /** 지정가 주문일 때 설정; 시장가면 null */
    @Column(name = "limit_price", precision = 20, scale = 4)
    private BigDecimal limitPrice;

    /** 체결된 수량 (초기값 0) */
    @Column(name = "filled_quantity", nullable = false, precision = 20, scale = 8)
    private BigDecimal filledQuantity;

    /** 평균 체결 단가 (체결 후 설정) */
    @Column(name = "avg_fill_price", precision = 20, scale = 4)
    private BigDecimal avgFillPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    /** 멱등성 키 — 동일 참조 ID 로 중복 주문 방지 */
    @Column(name = "reference_id", nullable = false, length = 64)
    private String referenceId;

    @Column(name = "ordered_at", nullable = false)
    private Instant orderedAt;

    @Column(name = "filled_at")
    private Instant filledAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    // ── 팩토리 ────────────────────────────────────────────────────

    public static StockOrder place(Long accountId, Long stockId,
                                    OrderSide side, OrderType orderType,
                                    BigDecimal quantity, BigDecimal limitPrice,
                                    String referenceId, Instant orderedAt) {
        StockOrder o = new StockOrder();
        o.accountId      = accountId;
        o.stockId        = stockId;
        o.side           = side;
        o.orderType      = orderType;
        o.quantity       = quantity;
        o.limitPrice     = limitPrice;
        o.filledQuantity = BigDecimal.ZERO;
        o.status         = OrderStatus.PENDING;
        o.referenceId    = referenceId;
        o.orderedAt      = orderedAt;
        return o;
    }

    // ── 도메인 행위 ──────────────────────────────────────────────

    /**
     * 전량 체결 처리 — 단일 체결 모델.
     *
     * @param executionPrice 실제 체결 단가
     */
    public void fill(BigDecimal executionPrice) {
        if (this.status == OrderStatus.FILLED) throw new OrderAlreadyExecutedException();
        if (this.status != OrderStatus.PENDING) throw new OrderNotPendingException();
        this.filledQuantity = this.quantity;
        this.avgFillPrice   = executionPrice.setScale(4, RoundingMode.HALF_UP);
        this.status         = OrderStatus.FILLED;
        this.filledAt       = Instant.now();
    }

    /** 주문 취소 — PENDING 상태에서만 가능 */
    public void cancel() {
        if (this.status != OrderStatus.PENDING) throw new OrderNotPendingException();
        this.status      = OrderStatus.CANCELLED;
        this.cancelledAt = Instant.now();
    }

    public BigDecimal remainingQuantity() {
        return this.quantity.subtract(this.filledQuantity);
    }
}
