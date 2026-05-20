package com.revy.example.domain.fx;

import com.revy.example.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "currency",
    uniqueConstraints = @UniqueConstraint(name = "uq_currency_code", columnNames = "code"),
    indexes = @Index(name = "idx_currency_active", columnList = "is_active")
)
public class Currency extends BaseEntity {

    /** ISO 4217 통화 코드 (KRW, USD, JPY, EUR ...) */
    @Column(name = "code", nullable = false, length = 3)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /** 통화 기호 (₩, $, ¥, € ...) */
    @Column(name = "symbol", nullable = false, length = 10)
    private String symbol;

    /** 소수점 자릿수 (KRW=0, JPY=0, USD=2, EUR=2, BHD=3) */
    @Column(name = "decimal_places", nullable = false)
    private Integer decimalPlaces;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // ── 팩토리 ────────────────────────────────────────────────────
    public static Currency register(String code, String name, String symbol, Integer decimalPlaces) {
        Currency c = new Currency();
        c.code           = code;
        c.name           = name;
        c.symbol         = symbol;
        c.decimalPlaces  = decimalPlaces;
        c.isActive       = true;
        return c;
    }

    // ── 비즈니스 ──────────────────────────────────────────────────
    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }

    public void updateMetadata(String name, String symbol) {
        this.name   = name;
        this.symbol = symbol;
    }
}
