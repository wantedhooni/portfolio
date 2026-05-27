package com.revy.example.domain.fx;

import com.revy.example.domain.common.BaseEntity;
import com.revy.example.domain.fx.enums.CorridorStatus;
import com.revy.example.domain.fx.exception.FxAmountBelowMinException;
import com.revy.example.domain.fx.exception.FxAmountExceedsMaxException;
import com.revy.example.domain.fx.exception.FxCorridorNotActiveException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 환전 구간 (FX Corridor).
 *
 * <p>특정 통화쌍의 환전 가능 조건을 정의합니다.
 * <ul>
 *   <li>건당 최소·최대 금액 ({@code minAmount} / {@code maxAmount})</li>
 *   <li>1일 한도 ({@code dailyLimit}, {@code null} = 무제한)</li>
 *   <li>스프레드율 ({@code spreadRate}) — 시장 환율에 마진을 얹어 고객 환율을 산출</li>
 * </ul>
 *
 * <p>동일 통화쌍(base + quote)은 하나의 Corridor만 허용합니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "fx_corridor",
    uniqueConstraints = @UniqueConstraint(
        name  = "uq_fx_corridor_pair",
        columnNames = {"base_currency_code", "quote_currency_code"}
    ),
    indexes = {
        @Index(name = "idx_corridor_status", columnList = "status"),
        @Index(name = "idx_corridor_pair",   columnList = "base_currency_code,quote_currency_code")
    }
)
public class FxCorridor extends BaseEntity {

    /** 기준 통화 코드 (예: USD) */
    @Column(name = "base_currency_code", nullable = false, length = 3)
    private String baseCurrencyCode;

    /** 인용 통화 코드 (예: KRW) */
    @Column(name = "quote_currency_code", nullable = false, length = 3)
    private String quoteCurrencyCode;

    /** 건당 최소 환전 금액 (기준 통화) */
    @Column(name = "min_amount", nullable = false, precision = 20, scale = 4)
    private BigDecimal minAmount;

    /** 건당 최대 환전 금액 (기준 통화, null = 무제한) */
    @Column(name = "max_amount", precision = 20, scale = 4)
    private BigDecimal maxAmount;

    /** 1일 누적 한도 (기준 통화, null = 무제한) */
    @Column(name = "daily_limit", precision = 20, scale = 4)
    private BigDecimal dailyLimit;

    /**
     * 스프레드율 (0.015 = 1.5%).
     * 고객 환율 = 시장 환율 × (1 - spreadRate)
     */
    @Column(name = "spread_rate", nullable = false, precision = 8, scale = 6)
    private BigDecimal spreadRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CorridorStatus status;

    // ── 팩토리 ────────────────────────────────────────────────────

    public static FxCorridor create(String baseCurrencyCode,
                                    String quoteCurrencyCode,
                                    BigDecimal minAmount,
                                    BigDecimal maxAmount,
                                    BigDecimal dailyLimit,
                                    BigDecimal spreadRate) {
        FxCorridor c = new FxCorridor();
        c.baseCurrencyCode  = baseCurrencyCode;
        c.quoteCurrencyCode = quoteCurrencyCode;
        c.minAmount         = minAmount;
        c.maxAmount         = maxAmount;
        c.dailyLimit        = dailyLimit;
        c.spreadRate        = spreadRate;
        c.status            = CorridorStatus.INACTIVE; // 생성 후 명시적으로 활성화
        return c;
    }

    // ── 비즈니스 ──────────────────────────────────────────────────

    /**
     * 환전 금액이 구간 조건에 부합하는지 검증합니다.
     *
     * @throws FxAmountBelowMinException  최소 금액 미달
     * @throws FxAmountExceedsMaxException 최대 금액 초과
     */
    public void validateAmount(BigDecimal amount) {
        if (amount.compareTo(minAmount) < 0) {
            throw new FxAmountBelowMinException(amount, minAmount);
        }
        if (maxAmount != null && amount.compareTo(maxAmount) > 0) {
            throw new FxAmountExceedsMaxException(amount, maxAmount);
        }
    }

    /**
     * 시장 환율에 스프레드를 적용하여 고객 환율을 반환합니다.
     *
     * <p>{@code customerRate = marketRate × (1 - spreadRate)}
     *
     * @param marketRate 시장(기준) 환율
     * @return 고객 적용 환율 (소수점 8자리)
     * @throws FxCorridorNotActiveException 구간이 활성 상태가 아닌 경우
     */
    public BigDecimal applySpread(BigDecimal marketRate) {
        if (status != CorridorStatus.ACTIVE) {
            throw new FxCorridorNotActiveException(baseCurrencyCode, quoteCurrencyCode);
        }
        BigDecimal multiplier = BigDecimal.ONE.subtract(spreadRate);
        return marketRate.multiply(multiplier).setScale(8, RoundingMode.HALF_UP);
    }

    public void activate() {
        this.status = CorridorStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = CorridorStatus.INACTIVE;
    }

    public void suspend() {
        this.status = CorridorStatus.SUSPENDED;
    }

    public void update(BigDecimal minAmount,
                       BigDecimal maxAmount,
                       BigDecimal dailyLimit,
                       BigDecimal spreadRate) {
        this.minAmount   = minAmount;
        this.maxAmount   = maxAmount;
        this.dailyLimit  = dailyLimit;
        this.spreadRate  = spreadRate;
    }
}
