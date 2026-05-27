package com.revy.example.domain.fx;

import com.revy.example.domain.common.BaseEntity;
import com.revy.example.domain.fx.enums.RateType;
import com.revy.example.domain.fx.exception.InvalidFxPairException;
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
import java.time.Instant;

/**
 * 통화쌍별 현재(최신) 환율.
 *
 * <p>(baseCurrencyCode, quoteCurrencyCode, rateType) 조합에 대해 항상 1행을 유지합니다.
 * 새 시세가 들어오면 {@link #refresh}로 in-place 갱신합니다.
 *
 * <p><b>저장 흐름:</b>
 * <ol>
 *   <li>{@link ExchangeRateHistory}에 이력 INSERT</li>
 *   <li>이 테이블에 UPSERT (없으면 INSERT, 있으면 UPDATE)</li>
 * </ol>
 *
 * @see ExchangeRateHistory
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "exchange_rate",
    uniqueConstraints = @UniqueConstraint(
        name         = "uq_exchange_rate_pair_type",
        columnNames  = {"base_currency_code", "quote_currency_code", "rate_type"}
    ),
    indexes = @Index(name = "idx_exchange_rate_pair", columnList = "base_currency_code,quote_currency_code")
)
public class ExchangeRate extends BaseEntity {

    /** 기준 통화 (예: USD) */
    @Column(name = "base_currency_code", nullable = false, length = 3)
    private String baseCurrencyCode;

    /** 인용 통화 (예: KRW) */
    @Column(name = "quote_currency_code", nullable = false, length = 3)
    private String quoteCurrencyCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "rate_type", nullable = false, length = 20)
    private RateType rateType;

    /** 1 base = rate quote */
    @Column(name = "rate", nullable = false, precision = 18, scale = 8)
    private BigDecimal rate;

    /** 시세 시각 (외부 데이터 소스 기준) */
    @Column(name = "quoted_at", nullable = false)
    private Instant quotedAt;

    /** 시세 제공처 */
    @Column(name = "source", nullable = false, length = 50)
    private String source;

    // ── 팩토리 ────────────────────────────────────────────────────

    public static ExchangeRate of(String baseCode, String quoteCode,
                                  RateType type, BigDecimal rate,
                                  Instant quotedAt, String source) {
        if (baseCode == null || quoteCode == null || baseCode.equals(quoteCode)) {
            throw new InvalidFxPairException("%s/%s".formatted(baseCode, quoteCode));
        }
        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidFxPairException("rate must be positive: " + rate);
        }
        ExchangeRate r = new ExchangeRate();
        r.baseCurrencyCode  = baseCode;
        r.quoteCurrencyCode = quoteCode;
        r.rateType          = type;
        r.rate              = rate;
        r.quotedAt          = quotedAt;
        r.source            = source;
        return r;
    }

    // ── 비즈니스 ──────────────────────────────────────────────────

    /** 새 시세 수신 시 현재 환율 갱신 */
    public void refresh(BigDecimal newRate, Instant newQuotedAt, String newSource) {
        if (newRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidFxPairException("rate must be positive: " + newRate);
        }
        this.rate      = newRate;
        this.quotedAt  = newQuotedAt;
        this.source    = newSource;
    }

    /** baseAmount 만큼의 base 통화를 quote 통화로 환산 */
    public BigDecimal convertToQuote(BigDecimal baseAmount) {
        return baseAmount.multiply(this.rate);
    }

    /** quoteAmount 만큼의 quote 통화를 base 통화로 환산 */
    public BigDecimal convertToBase(BigDecimal quoteAmount, int scale) {
        return quoteAmount.divide(this.rate, scale, java.math.RoundingMode.HALF_UP);
    }
}
