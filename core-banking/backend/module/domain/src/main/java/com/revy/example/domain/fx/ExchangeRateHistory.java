package com.revy.example.domain.fx;

import com.revy.example.domain.common.BaseEntity;
import com.revy.example.domain.fx.enums.RateType;
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

/**
 * 환율 이력 (append-only).
 *
 * <p>새 시세가 수신될 때마다 이 테이블에 INSERT합니다.
 * 현재 환율은 {@link ExchangeRate}에서 관리하며, 이 테이블은 감사(audit) 및 이력 조회 전용입니다.
 *
 * @see ExchangeRate
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "exchange_rate_history",
    indexes = {
        @Index(name = "idx_erh_pair_type_at",
               columnList = "base_currency_code,quote_currency_code,rate_type,quoted_at"),
        @Index(name = "idx_erh_quoted_at", columnList = "quoted_at")
    }
)
public class ExchangeRateHistory extends BaseEntity {

    @Column(name = "base_currency_code", nullable = false, length = 3)
    private String baseCurrencyCode;

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

    public static ExchangeRateHistory record(String baseCode, String quoteCode,
                                             RateType type, BigDecimal rate,
                                             Instant quotedAt, String source) {
        ExchangeRateHistory h = new ExchangeRateHistory();
        h.baseCurrencyCode  = baseCode;
        h.quoteCurrencyCode = quoteCode;
        h.rateType          = type;
        h.rate              = rate;
        h.quotedAt          = quotedAt;
        h.source            = source;
        return h;
    }
}
