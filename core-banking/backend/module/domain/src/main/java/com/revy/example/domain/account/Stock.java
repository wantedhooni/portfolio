package com.revy.example.domain.account;


import com.revy.example.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "stock", uniqueConstraints = @UniqueConstraint(name = "uq_stock_ticker_exchange", columnNames = {"ticker", "exchange"}        // 동일 티커가 여러 거래소에 상장 가능
), indexes = {@Index(name = "idx_stock_exchange", columnList = "exchange"), @Index(name = "idx_stock_sector", columnList = "sector"), @Index(name = "idx_stock_is_active", columnList = "is_active")})
public class Stock extends BaseEntity {

    @Column(name = "ticker", nullable = false, length = 20)
    private String ticker;              // "005930", "AAPL"

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "exchange", nullable = false, length = 10)
    private String exchange;            // "KRX", "NASDAQ", "NYSE"

    @Column(name = "sector", length = 100)
    private String sector;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    // ── 캐시성 시장 데이터 (배치/스케줄러 갱신) ──────────────────
    @Column(name = "last_price", precision = 20, scale = 4)
    private BigDecimal lastPrice;

    @Column(name = "market_cap", precision = 24, scale = 4)
    private BigDecimal marketCap;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    // ── 팩토리 ────────────────────────────────────────────────────
    public static Stock create(String ticker, String name, String exchange, String sector, String currency) {
        Stock s = new Stock();
        s.ticker    = ticker;
        s.name      = name;
        s.exchange  = exchange;
        s.sector    = sector;
        s.currency  = currency;
        s.isActive  = true;
        return s;
    }

    // ── 비즈니스 ──────────────────────────────────────────────────
    public void updateMarketData(BigDecimal lastPrice, BigDecimal marketCap) {
        this.lastPrice = lastPrice;
        this.marketCap = marketCap;
        this.lastSyncedAt = Instant.now();
    }

    public void delist() {
        this.isActive = false;
    }
}