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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "exchange_rate",
    indexes = {
        @Index(name = "idx_fx_pair_type_at",
               columnList = "base_currency_code,quote_currency_code,rate_type,quoted_at"),
        @Index(name = "idx_fx_quoted_at", columnList = "quoted_at")
    }
)
public class ExchangeRate extends BaseEntity {

    /** 기준 통화 (예: USD/KRW에서 USD) — 코드로 저장 (조회 효율) */
    @Column(name = "base_currency_code", nullable = false, length = 3)
    private String baseCurrencyCode;

    /** 인용 통화 (예: USD/KRW에서 KRW) */
    @Column(name = "quote_currency_code", nullable = false, length = 3)
    private String quoteCurrencyCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "rate_type", nullable = false, length = 20)
    private RateType rateType;

    /** 1 base = rate quote (예: 1 USD = 1380.50 KRW) */
    @Column(name = "rate", nullable = false, precision = 18, scale = 8)
    private BigDecimal rate;

    /** 시세 시각 (외부 데이터 소스 기준) */
    @Column(name = "quoted_at", nullable = false)
    private Instant quotedAt;

    /** 시세 제공처 (한국은행, KEB하나, 자체 호가 등) */
    @Column(name = "source", nullable = false, length = 50)
    private String source;

    // ── 팩토리 ────────────────────────────────────────────────────
    public static ExchangeRate quote(String baseCode, String quoteCode, RateType type,
                                     BigDecimal rate, Instant quotedAt, String source) {
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

    /**
     * baseAmount 만큼의 base 통화를 quote 통화로 환산.
     * 결과는 quote 통화 기준 — 소수점 처리는 호출부 책임 (통화별 decimal_places 적용).
     */
    public BigDecimal convertToQuote(BigDecimal baseAmount) {
        return baseAmount.multiply(this.rate);
    }

    /**
     * quoteAmount 만큼의 quote 통화를 base 통화로 환산.
     */
    public BigDecimal convertToBase(BigDecimal quoteAmount, int scale) {
        return quoteAmount.divide(this.rate, scale, java.math.RoundingMode.HALF_UP);
    }
}
