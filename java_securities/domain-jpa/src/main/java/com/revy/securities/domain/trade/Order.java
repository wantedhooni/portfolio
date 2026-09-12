package com.revy.securities.domain.trade;

import com.revy.securities.domain.common.BaseUUIDEntity;
import com.revy.securities.domain.trade.enums.OrderStatus;
import com.revy.securities.domain.trade.enums.OrderType;
import com.revy.securities.domain.trade.enums.Side;
import com.revy.securities.domain.trade.enums.TimeInForce;
import com.revy.common.enums.Currency;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 주문(Order)
 * - 사용자가 낸 주문 요청(의도/요청)을 저장한다.
 * - 실제 체결 결과는 OrderFill에 누적된다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "stock_order", indexes = {})
public class Order extends BaseUUIDEntity {

    @Column(name = "owner_id", nullable = false)
    private Long ownerId; // 요청자

    @Column(name = "account_id", nullable = false)
    private Long accountId; // 주문을 낸 계좌 ID(계좌 소유자/잔고 기준)

    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol; // 종목 코드(예: AAPL, 005930)

    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false, length = 10)
    private Side side; // 매수/매도 방향

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private OrderType type; // 주문 타입(시장가/지정가)

    @Enumerated(EnumType.STRING)
    @Column(name = "tif", nullable = false, length = 20)
    private TimeInForce tif; // 주문 유효기간(예: DAY, GTC)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status; // 주문 상태(NEW, PARTIALLY_FILLED, ...)

    @Column(name = "qty", nullable = false, precision = 19, scale = 6)
    private BigDecimal qty; // 주문 수량(부분주 고려 시 소수 가능)

    @Column(name = "limit_price_amount", precision = 19, scale = 6)
    private BigDecimal limitPriceAmount; // 지정가(지정가 주문일 때만 사용, 시장가면 null)


    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    private Currency currency; // 주문 가격 통화(예: KRW, USD)

    @Column(name = "filled_qty", nullable = false, precision = 19, scale = 6)
    private BigDecimal filledQty; // 누적 체결 수량(합계)

    @Column(name = "avg_fill_price_amount", precision = 19, scale = 6)
    private BigDecimal avgFillPriceAmount; // 누적 평균 체결 단가(가중평균)

    @Column(name="cancel_at")
    private Instant cancelAt;
    /**
     * 주문 생성(심플 팩토리)
     */
    public static Order create(Long ownerId,
                               Long accountId,
                               String symbol,
                               Side side,
                               OrderType type,
                               TimeInForce tif,
                               BigDecimal qty,
                               BigDecimal limitPriceAmount,
                               Currency currency
    ) {
        Order o = new Order();
        o.ownerId = ownerId;
        o.accountId = accountId;
        o.symbol = symbol;
        o.side = side;
        o.type = type;
        o.tif = tif;
        o.status = OrderStatus.NEW;
        o.qty = qty;
        o.limitPriceAmount = limitPriceAmount;
        o.currency = currency;
        o.filledQty = BigDecimal.ZERO;
        o.avgFillPriceAmount = BigDecimal.ZERO;
        return o;
    }


    /**
     * 주문 취소 처리(미체결 또는 부분체결 상태에서만 허용)
     */
    public void cancel() {
        if (!this.status.isCancleable()) {
            throw new IllegalStateException("cannot cancel finalized order");
        }
        this.status = OrderStatus.CANCELED;
        this.cancelAt = Instant.now();
    }
}
