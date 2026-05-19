package com.revy.example.domain.account;

import com.revy.example.domain.account.enums.AccountStatus;
import com.revy.example.domain.account.enums.AccountType;
import com.revy.example.domain.account.exception.AccountNotActiveException;
import com.revy.example.domain.account.exception.InsufficientBalanceException;
import com.revy.example.domain.common.BaseEntity;
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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "account",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_account_number",
        columnNames = "account_number"
    ),
    indexes = @Index(name = "idx_account_user_id", columnList = "user_id")
)
public class Account extends BaseEntity {

    /**
     * 낙관적 락 버전
     * balance / availableBalance는 동시 주문·입출금에서 충돌 가능성이 높다.
     * @Version으로 충돌을 감지하고 OptimisticLockException → 호출부에서 재시도 처리.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "account_number", nullable = false, length = 20)
    private String accountNumber;

    @Column(name = "account_name", nullable = false, length = 100)
    private String accountName;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;        // REAL / VIRTUAL

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;               // "KRW", "USD"

    @Column(name = "balance", nullable = false, precision = 20, scale = 4)
    private BigDecimal balance;

    @Column(name = "available_balance", nullable = false, precision = 20, scale = 4)
    private BigDecimal availableBalance;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AccountStatus status;          // ACTIVE / SUSPENDED / CLOSED

    // ── 팩토리 ────────────────────────────────────────────────────
    public static Account open(Long userId, String accountNumber,
                               String accountName, AccountType type, String currency) {
        Account a = new Account();
        a.userId           = userId;
        a.accountNumber    = accountNumber;
        a.accountName      = accountName;
        a.accountType      = type;
        a.currency         = currency;
        a.balance          = BigDecimal.ZERO;
        a.availableBalance = BigDecimal.ZERO;
        a.status           = AccountStatus.ACTIVE;
        return a;
    }

    // ── 비즈니스 ──────────────────────────────────────────────────
    public void deposit(BigDecimal amount) {
        validateActive();
        this.balance          = this.balance.add(amount);
        this.availableBalance = this.availableBalance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        validateActive();
        if (this.availableBalance.compareTo(amount) < 0)
            throw new InsufficientBalanceException();
        this.balance          = this.balance.subtract(amount);
        this.availableBalance = this.availableBalance.subtract(amount);
    }

    /** 주문 제출 시 가용잔고 선점 */
    public void reserveForOrder(BigDecimal amount) {
        validateActive();
        if (this.availableBalance.compareTo(amount) < 0)
            throw new InsufficientBalanceException();
        this.availableBalance = this.availableBalance.subtract(amount);
    }

    /** 체결 확정 시 실잔고 차감 */
    public void confirmBuy(BigDecimal amount) {
        validateActive();
        this.balance = this.balance.subtract(amount);
    }

    /** 매도 대금 입금 */
    public void creditSaleProceeds(BigDecimal proceeds) {
        validateActive();
        this.balance          = this.balance.add(proceeds);
        this.availableBalance = this.availableBalance.add(proceeds);
    }

    /** 주문 취소 시 가용잔고 복원 */
    public void releaseReservation(BigDecimal amount) {
        validateActive();
        this.availableBalance = this.availableBalance.add(amount);
    }

    /** 배당금 등 시스템 입금 — 계좌 상태와 무관하게 입금 처리 (CLOSED는 호출부에서 차단) */
    public void creditDividend(BigDecimal netAmount) {
        this.balance          = this.balance.add(netAmount);
        this.availableBalance = this.availableBalance.add(netAmount);
    }

    public void suspend() {
        this.status = AccountStatus.SUSPENDED;
    }

    public void close() {
        this.status = AccountStatus.CLOSED;
    }

    public void updateName(String accountName) {
        this.accountName = accountName;
    }

    private void validateActive() {
        if (this.status != AccountStatus.ACTIVE)
            throw new AccountNotActiveException();
    }
}