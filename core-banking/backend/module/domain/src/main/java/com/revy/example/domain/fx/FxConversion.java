package com.revy.example.domain.fx;

import com.revy.example.domain.common.BaseEntity;
import com.revy.example.domain.fx.enums.FxStatus;
import com.revy.example.domain.fx.enums.RateType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "fx_conversion",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_fx_conversion_number", columnNames = "conversion_number"),
        @UniqueConstraint(name = "uq_fx_reference_id",      columnNames = "reference_id")
    },
    indexes = {
        @Index(name = "idx_fx_from_account", columnList = "from_account_id"),
        @Index(name = "idx_fx_to_account",   columnList = "to_account_id"),
        @Index(name = "idx_fx_executed_at",  columnList = "executed_at"),
        @Index(name = "idx_fx_status",       columnList = "status")
    }
)
public class FxConversion extends BaseEntity {

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "conversion_number", nullable = false, length = 30)
    private String conversionNumber;

    @Column(name = "from_account_id", nullable = false)
    private Long fromAccountId;

    @Column(name = "to_account_id", nullable = false)
    private Long toAccountId;

    @Column(name = "from_currency_code", nullable = false, length = 3)
    private String fromCurrencyCode;

    @Column(name = "to_currency_code", nullable = false, length = 3)
    private String toCurrencyCode;

    /** 출금 통화 기준 금액 (예: USD 1000) */
    @Column(name = "from_amount", nullable = false, precision = 20, scale = 4)
    private BigDecimal fromAmount;

    /** 입금 통화 기준 금액 (예: KRW 1,380,500) */
    @Column(name = "to_amount", nullable = false, precision = 20, scale = 4)
    private BigDecimal toAmount;

    /** 적용된 환율 (snapshot — 시세 변동 후에도 추적 가능) */
    @Column(name = "applied_rate", nullable = false, precision = 18, scale = 8)
    private BigDecimal appliedRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "applied_rate_type", nullable = false, length = 20)
    private RateType appliedRateType;

    /** 환전 수수료 (입금 통화 기준) */
    @Column(name = "fee", nullable = false, precision = 18, scale = 4)
    private BigDecimal fee;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FxStatus status;

    /** 출금 AccountTx FK */
    @Column(name = "debit_tx_id")
    private Long debitTxId;

    /** 입금 AccountTx FK */
    @Column(name = "credit_tx_id")
    private Long creditTxId;

    @Column(name = "executed_at")
    private Instant executedAt;

    @Column(name = "failure_reason", length = 200)
    private String failureReason;

    /** 멱등성 키 */
    @Column(name = "reference_id", nullable = false, length = 64)
    private String referenceId;

    // ── 팩토리 ────────────────────────────────────────────────────
    public static FxConversion request(String conversionNumber,
                                       Long fromAccountId, Long toAccountId,
                                       String fromCurrency, String toCurrency,
                                       BigDecimal fromAmount, BigDecimal toAmount,
                                       BigDecimal appliedRate, RateType rateType,
                                       BigDecimal fee, String referenceId) {
        FxConversion fx = new FxConversion();
        fx.conversionNumber  = conversionNumber;
        fx.fromAccountId     = fromAccountId;
        fx.toAccountId       = toAccountId;
        fx.fromCurrencyCode  = fromCurrency;
        fx.toCurrencyCode    = toCurrency;
        fx.fromAmount        = fromAmount;
        fx.toAmount          = toAmount;
        fx.appliedRate       = appliedRate;
        fx.appliedRateType   = rateType;
        fx.fee               = fee;
        fx.referenceId       = referenceId;
        fx.status            = FxStatus.PENDING;
        return fx;
    }

    // ── 비즈니스 ──────────────────────────────────────────────────
    public void complete(Long debitTxId, Long creditTxId, Instant now) {
        this.debitTxId   = debitTxId;
        this.creditTxId  = creditTxId;
        this.executedAt  = now;
        this.status      = FxStatus.COMPLETED;
    }

    public void fail(String reason) {
        this.status         = FxStatus.FAILED;
        this.failureReason  = reason;
    }

    public void cancel() {
        if (this.status != FxStatus.PENDING) return;
        this.status = FxStatus.CANCELLED;
    }
}
