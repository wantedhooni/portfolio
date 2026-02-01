package com.revy.securities.domain.trade;

import com.revy.securities.domain.common.BaseUUIDEntity;
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
@Table(name = "stock_order_fill",
        indexes = {
        })
public class OrderFill extends BaseUUIDEntity {

    @Column(name = "account_id", nullable = false)
    private Long accountId; // 계좌 ID(조회/정산 편의상 중복 저장)

    @Column(name = "order_id", nullable = false)
    private UUID orderId; // 어떤 주문에 대한 체결인지(주문 ID)

    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol; // 종목 코드

    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false, length = 10)
    private Side side; // 매수/매도(주문과 동일)

    @Column(name = "qty", nullable = false, precision = 19, scale = 6)
    private BigDecimal qty; // 이번 체결 수량

    @Column(name = "price_amount", nullable = false, precision = 19, scale = 6)
    private BigDecimal priceAmount; // 체결 단가(금액)

    @Column(name = "currency", nullable = false, length = 3)
    private Currency currency; // 체결 단가 통화

    @Column(name = "fee_amount", nullable = false, precision = 19, scale = 6)
    private BigDecimal feeAmount; // 수수료(없으면 0)

    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 6)
    private BigDecimal taxAmount; // 세금(없으면 0)

    @Column(name = "executed_at", nullable = false)
    private Instant executedAt; // 체결 시각

    /**
     * 체결 생성(심플 팩토리)
     */
    public static OrderFill create(UUID orderId,
                                   Long accountId,
                                   String symbol,
                                   Side side,
                                   BigDecimal qty,
                                   BigDecimal priceAmount,
                                   Currency priceCurrency,
                                   BigDecimal feeAmount,
                                   BigDecimal taxAmount,
                                   Instant executedAt) {
        OrderFill f = new OrderFill();
        f.orderId = orderId;
        f.accountId = accountId;
        f.symbol = symbol;
        f.side = side;
        f.qty = qty;
        f.priceAmount = priceAmount;
        f.currency = priceCurrency;
        f.feeAmount = feeAmount == null ? BigDecimal.ZERO : feeAmount;
        f.taxAmount = taxAmount == null ? BigDecimal.ZERO : taxAmount;
        f.executedAt = executedAt;
        return f;
    }
}
