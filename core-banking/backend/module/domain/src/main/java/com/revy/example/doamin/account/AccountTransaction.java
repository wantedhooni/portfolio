package com.revy.example.doamin.account;

import com.revy.example.doamin.account.enums.TransactionType;
import com.revy.example.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "account_transaction")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter(AccessLevel.PUBLIC)
public class AccountTransaction extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 0)  // 원화: 소수점 없음
    private BigDecimal amount;

    /**
     * 거래 후 잔액 스냅샷 — 내역 조회 시 실시간 계산 없이 빠르게 표시
     */
    @Column(nullable = false, precision = 19, scale = 0)  // 원화: 소수점 없음
    private BigDecimal balanceSnapshot;

    @Column(length = 200)
    private String description;

    // ── 생성 팩토리 메서드 ──────────────────────────────
    public static AccountTransaction ofDeposit(Account account,
                                               BigDecimal amount,
                                               BigDecimal balanceAfter,
                                               String description) {
        return create(account, TransactionType.DEPOSIT, amount, balanceAfter, description);
    }

    public static AccountTransaction ofWithdraw(Account account,
                                                BigDecimal amount,
                                                BigDecimal balanceAfter,
                                                String description) {
        return create(account, TransactionType.WITHDRAW, amount, balanceAfter, description);
    }

    private static AccountTransaction create(Account account,
                                             TransactionType type,
                                             BigDecimal amount,
                                             BigDecimal balanceAfter,
                                             String description) {
        AccountTransaction tx = new AccountTransaction();
        tx.account = account;
        tx.type = type;
        tx.amount = amount;
        tx.balanceSnapshot = balanceAfter;
        tx.description = description;
        return tx;
    }
}
