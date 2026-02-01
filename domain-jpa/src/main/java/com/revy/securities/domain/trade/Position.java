package com.revy.securities.domain.trade;

import com.revy.securities.domain.common.BaseUUIDEntity;
import com.revy.securities.domain.trade.enums.PositionStatus;
import com.revy.common.enums.Currency;
import com.revy.common.utils.BigDecimalUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "stock_position", indexes = {})
public class Position extends BaseUUIDEntity {

    @Column(name = "account_id", nullable = false)
    private Long accountId; // 계좌 ID(조회/정산 편의상 중복 저장)

    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 6)
    private BigDecimal quantity;

    @Column(name = "avg_price_amount", nullable = false, precision = 19, scale = 6)
    private BigDecimal avgPriceAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "avg_price_currency", nullable = false, length = 3)
    private Currency avgPriceCurrency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private PositionStatus status;

    @Column(name = "closed_at")
    private Instant closedAt;

    public static Position open(Long accountId, String symbol, Currency currency) {
        Position p = new Position();
        p.accountId = accountId;
        p.symbol = symbol;
        p.quantity = BigDecimal.ZERO;
        p.avgPriceAmount = BigDecimal.ZERO;
        p.avgPriceCurrency = currency;
        p.open();
        return p;
    }

    /**
     * 매수 체결 반영: 수량 증가 + 평단(가중평균) 갱신
     */
    public void applyBuy(BigDecimal buyQty, BigDecimal buyPriceAmount, Currency currency) {
        BigDecimal prevQty = this.quantity;
        BigDecimal newQty = prevQty.add(buyQty);
        BigDecimal totalCost = this.avgPriceAmount.multiply(prevQty).add(buyPriceAmount.multiply(buyQty));
        this.avgPriceAmount = totalCost.divide(newQty, currency.getDigits(), RoundingMode.HALF_UP);
        this.quantity = newQty;
        this.open();
    }

    /**
     * 매도 체결 반영: 수량 감소
     *
     * @return 실현손익(= (sell - avg) * qty) (수수료/세금은 서비스에서 반영 권장)
     */
    public BigDecimal applySell(BigDecimal sellQty, BigDecimal sellPriceAmount) {
        if (BigDecimalUtil.isGreaterThan(sellQty, this.quantity)) {
            throw new IllegalStateException("insufficient position quantity");
        }
        BigDecimal pnl = sellPriceAmount.subtract(this.avgPriceAmount)
                                        .multiply(sellQty)
                                        .setScale(avgPriceCurrency.getDigits(), RoundingMode.HALF_DOWN);
        this.quantity = this.quantity.subtract(sellQty);
        close();
        return pnl;
    }

    private void close() {
        if (BigDecimalUtil.isEquals(this.quantity, BigDecimal.ZERO)) {
            this.status = PositionStatus.CLOSED;
            this.closedAt = Instant.now();
        }
    }

    private void open() {
        this.status = PositionStatus.OPEN;
        this.closedAt = null;
    }
}
