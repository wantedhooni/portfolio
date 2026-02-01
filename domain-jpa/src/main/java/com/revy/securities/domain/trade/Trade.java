package com.revy.securities.domain.trade;

import com.revy.securities.domain.common.BaseUUIDEntity;
import com.revy.securities.domain.trade.OrderFill;
import com.revy.securities.domain.trade.enums.Side;
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
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "stock_trade", indexes = {})
public class Trade extends BaseUUIDEntity {

    @Column(name = "account_id", nullable = false)
    private Long accountId; // 계좌 ID(조회/정산 편의상 중복 저장)

    @Column(name = "order_id", nullable = false)
    private UUID orderId; // 원 주문 ID

    @Column(name = "fill_id", nullable = false)
    private UUID fillId; // 원 체결 ID(중복 저장 방지/추적)

    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false, length = 10)
    private Side side;

    @Column(name = "qty", nullable = false, precision = 19, scale = 6)
    private BigDecimal qty; // 체결 수량

    @Column(name = "price_amount", nullable = false, precision = 19, scale = 6)
    private BigDecimal priceAmount; // 체결 단가(금액)

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    private Currency currency; // 단가 통화 (예: KRW, USD)

    @Column(name = "fee_amount", nullable = false, precision = 19, scale = 6)
    private BigDecimal feeAmount; // 수수료(없으면 0)

    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 6)
    private BigDecimal taxAmount; // 세금(없으면 0)

    @Column(name = "realized_pnl_amount", precision = 19, scale = 6)
    private BigDecimal realizedPnlAmount; // 매도 시 실현손익(선택)

    @Column(name = "executed_at", nullable = false)
    private Instant executedAt;

    /**
     * OrderFill 기반으로 Trade 생성(미러링)
     */
    public static Trade fromFill(OrderFill fill) {
        Trade t = new Trade();
        t.accountId = fill.getAccountId();
        t.orderId = fill.getOrderId();
        t.fillId = fill.getId();
        t.symbol = fill.getSymbol();
        t.side = fill.getSide();
        t.qty = fill.getQty();
        t.priceAmount = fill.getPriceAmount();
        t.currency = fill.getCurrency();
        t.feeAmount = fill.getFeeAmount();
        t.taxAmount = fill.getTaxAmount();
        t.executedAt = fill.getExecutedAt();
        return t;
    }
}
