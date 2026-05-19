package com.revy.example.domain.account;

import com.revy.example.domain.account.enums.TxStatus;
import com.revy.example.domain.account.enums.TxType;
import com.revy.example.domain.common.BaseEntity;
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
    name = "account_tx",
    indexes = {
        @Index(name = "idx_account_tx_account_id",        columnList = "account_id"),
        @Index(name = "idx_account_tx_stock_id",          columnList = "stock_id"),
        @Index(name = "idx_account_tx_traded_at",         columnList = "traded_at"),
        @Index(name = "idx_account_tx_account_traded_at", columnList = "account_id,traded_at")
    }
)
public class AccountTx extends BaseEntity {

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    /** 매수/매도/배당 시에만 존재. 입출금·수수료·세금은 null */
    @Column(name = "stock_id")
    private Long stockId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tx_type", nullable = false, length = 20)
    private TxType txType;
    // DEPOSIT / WITHDRAWAL / BUY / SELL / DIVIDEND / FEE / TAX

    /** 거래 총금액 (BUY: price×qty+fee+tax, SELL: price×qty-fee-tax) */
    @Column(name = "amount", nullable = false, precision = 20, scale = 4)
    private BigDecimal amount;

    /** 주식 거래 시 수량 */
    @Column(name = "quantity", precision = 20, scale = 8)
    private BigDecimal quantity;

    /** 체결 단가 */
    @Column(name = "price", precision = 20, scale = 4)
    private BigDecimal price;

    @Column(name = "fee", nullable = false, precision = 10, scale = 4)
    private BigDecimal fee;

    @Column(name = "tax", nullable = false, precision = 10, scale = 4)
    private BigDecimal tax;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TxStatus status;            // PENDING / COMPLETED / CANCELLED / FAILED

    @Column(name = "reference_id", length = 64)
    private String referenceId;

    /** 실제 체결 시각 (createdAt과 구분 — 배치 적재 시 필수) */
    @Column(name = "traded_at", nullable = false)
    private Instant tradedAt;

    // ── 팩토리 메서드 ─────────────────────────────────────────────
    public static AccountTx ofDeposit(Long accountId, BigDecimal amount,
                                      String referenceId) {
        AccountTx tx = new AccountTx();
        tx.accountId   = accountId;
        tx.txType      = TxType.DEPOSIT;
        tx.amount      = amount;
        tx.fee         = BigDecimal.ZERO;
        tx.tax         = BigDecimal.ZERO;
        tx.status      = TxStatus.COMPLETED;
        tx.referenceId = referenceId;
        tx.tradedAt    = Instant.now();
        return tx;
    }

    public static AccountTx ofWithdrawal(Long accountId, BigDecimal amount,
                                         String referenceId) {
        AccountTx tx = new AccountTx();
        tx.accountId   = accountId;
        tx.txType      = TxType.WITHDRAWAL;
        tx.amount      = amount.negate();   // 출금은 음수
        tx.fee         = BigDecimal.ZERO;
        tx.tax         = BigDecimal.ZERO;
        tx.status      = TxStatus.COMPLETED;
        tx.referenceId = referenceId;
        tx.tradedAt    = Instant.now();
        return tx;
    }

    public static AccountTx ofBuy(Long accountId, Long stockId,
                                  BigDecimal quantity, BigDecimal price,
                                  BigDecimal fee, BigDecimal tax,
                                  String referenceId, Instant tradedAt) {
        AccountTx tx = new AccountTx();
        tx.accountId   = accountId;
        tx.stockId     = stockId;
        tx.txType      = TxType.BUY;
        tx.quantity    = quantity;
        tx.price       = price;
        tx.fee         = fee;
        tx.tax         = tax;
        tx.amount      = price.multiply(quantity).add(fee).add(tax).negate(); // 출금
        tx.status      = TxStatus.COMPLETED;
        tx.referenceId = referenceId;
        tx.tradedAt    = tradedAt;
        return tx;
    }

    public static AccountTx ofSell(Long accountId, Long stockId,
                                   BigDecimal quantity, BigDecimal price,
                                   BigDecimal fee, BigDecimal tax,
                                   String referenceId, Instant tradedAt) {
        AccountTx tx = new AccountTx();
        tx.accountId   = accountId;
        tx.stockId     = stockId;
        tx.txType      = TxType.SELL;
        tx.quantity    = quantity;
        tx.price       = price;
        tx.fee         = fee;
        tx.tax         = tax;
        tx.amount      = price.multiply(quantity).subtract(fee).subtract(tax); // 입금
        tx.status      = TxStatus.COMPLETED;
        tx.referenceId = referenceId;
        tx.tradedAt    = tradedAt;
        return tx;
    }

    public static AccountTx ofDividend(Long accountId, Long stockId,
                                       BigDecimal amount, BigDecimal tax,
                                       String referenceId, Instant tradedAt) {
        AccountTx tx = new AccountTx();
        tx.accountId   = accountId;
        tx.stockId     = stockId;
        tx.txType      = TxType.DIVIDEND;
        tx.amount      = amount;
        tx.fee         = BigDecimal.ZERO;
        tx.tax         = tax;
        tx.status      = TxStatus.COMPLETED;
        tx.referenceId = referenceId;
        tx.tradedAt    = tradedAt;
        return tx;
    }
}