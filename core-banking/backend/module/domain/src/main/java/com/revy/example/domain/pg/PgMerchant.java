package com.revy.example.domain.pg;

import com.revy.example.domain.common.BaseEntity;
import com.revy.example.domain.pg.exception.PgMerchantNotActiveException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * PG 가맹점.
 *
 * <p>가맹점별 수수료율({@code commissionRate})과 정산 주기({@code settlementCycle})를 관리한다.
 * 정산 배치는 {@code settlementCycle}일 전 승인된 결제를 대상으로 일괄 정산한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "pg_merchant",
    uniqueConstraints = @UniqueConstraint(name = "uq_pg_merchant_code", columnNames = "merchant_code"),
    indexes = {
        @Index(name = "idx_pg_merchant_active",   columnList = "is_active"),
        @Index(name = "idx_pg_merchant_biz_type", columnList = "business_type")
    }
)
public class PgMerchant extends BaseEntity {

    @Column(name = "merchant_code", nullable = false, length = 30)
    private String merchantCode;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /** 업종 분류 — RETAIL / FOOD / DIGITAL / TRAVEL / HEALTHCARE 등 */
    @Column(name = "business_type", nullable = false, length = 50)
    private String businessType;

    /** 정산 입금 계좌 (account 테이블 FK) */
    @Column(name = "settlement_account_id", nullable = false)
    private Long settlementAccountId;

    /** 수수료율 — 예: 0.0300 = 3% */
    @Column(name = "commission_rate", nullable = false, precision = 6, scale = 4)
    private BigDecimal commissionRate;

    /** 정산 주기 (일) — T+2 기본 */
    @Column(name = "settlement_cycle", nullable = false)
    private int settlementCycle;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "contact_email", length = 200)
    private String contactEmail;

    // ── 팩토리 ────────────────────────────────────────────────────

    public static PgMerchant create(
            String merchantCode, String name, String businessType,
            Long settlementAccountId, BigDecimal commissionRate,
            int settlementCycle, String currency, String contactEmail) {
        if (commissionRate == null || commissionRate.signum() < 0) {
            throw new IllegalArgumentException("commissionRate must be >= 0");
        }
        if (settlementCycle < 1) {
            throw new IllegalArgumentException("settlementCycle must be >= 1");
        }
        PgMerchant m = new PgMerchant();
        m.merchantCode         = merchantCode;
        m.name                 = name;
        m.businessType         = businessType;
        m.settlementAccountId  = settlementAccountId;
        m.commissionRate       = commissionRate;
        m.settlementCycle      = settlementCycle;
        m.currency             = currency;
        m.isActive             = true;
        m.contactEmail         = contactEmail;
        return m;
    }

    // ── 도메인 행위 ─────────────────────────────────────────────

    public void validateActive() {
        if (!this.isActive) {
            throw new PgMerchantNotActiveException();
        }
    }

    public void deactivate() { this.isActive = false; }
    public void activate()   { this.isActive = true; }

    public void updateCommissionRate(BigDecimal rate) {
        if (rate == null || rate.signum() < 0) {
            throw new IllegalArgumentException("commissionRate must be >= 0");
        }
        this.commissionRate = rate;
    }
}
