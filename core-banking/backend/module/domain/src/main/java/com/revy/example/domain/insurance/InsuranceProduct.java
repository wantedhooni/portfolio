package com.revy.example.domain.insurance;

import com.revy.example.domain.common.BaseEntity;
import com.revy.example.domain.insurance.enums.InsuranceType;
import com.revy.example.domain.insurance.enums.PremiumFrequency;
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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "insurance_product",
    uniqueConstraints = @UniqueConstraint(name = "uq_product_code", columnNames = "product_code"),
    indexes = {
        @Index(name = "idx_product_type", columnList = "insurance_type"),
        @Index(name = "idx_product_active", columnList = "is_active")
    }
)
public class InsuranceProduct extends BaseEntity {

    @Column(name = "product_code", nullable = false, length = 30)
    private String productCode;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "insurance_type", nullable = false, length = 20)
    private InsuranceType insuranceType;

    /** 기준 보험료 (월납 환산) */
    @Column(name = "base_premium", nullable = false, precision = 18, scale = 2)
    private BigDecimal basePremium;

    @Enumerated(EnumType.STRING)
    @Column(name = "premium_frequency", nullable = false, length = 20)
    private PremiumFrequency premiumFrequency;

    /** 보장 한도 — 청구·지급 시 상한 */
    @Column(name = "coverage_amount", nullable = false, precision = 20, scale = 2)
    private BigDecimal coverageAmount;

    /** 계약 기간 (개월) */
    @Column(name = "duration_months", nullable = false)
    private Integer durationMonths;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // ── 팩토리 ────────────────────────────────────────────────────
    public static InsuranceProduct create(String productCode, String name, String description,
                                          InsuranceType type, BigDecimal basePremium,
                                          PremiumFrequency frequency, BigDecimal coverageAmount,
                                          Integer durationMonths, String currency) {
        InsuranceProduct p = new InsuranceProduct();
        p.productCode       = productCode;
        p.name              = name;
        p.description       = description;
        p.insuranceType     = type;
        p.basePremium       = basePremium;
        p.premiumFrequency  = frequency;
        p.coverageAmount    = coverageAmount;
        p.durationMonths    = durationMonths;
        p.currency          = currency;
        p.isActive          = true;
        return p;
    }

    // ── 비즈니스 ──────────────────────────────────────────────────
    public void discontinue() {
        this.isActive = false;
    }

    public void updatePricing(BigDecimal basePremium, BigDecimal coverageAmount) {
        this.basePremium    = basePremium;
        this.coverageAmount = coverageAmount;
    }

    public void updateDescription(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
