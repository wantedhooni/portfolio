package com.revy.example.domain.account;

import com.revy.example.domain.account.enums.AccountStatus;
import com.revy.example.domain.bank.Bank;
import com.revy.example.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "account")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter(AccessLevel.PUBLIC)
public class Account extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id")  // 연관관계 주인 (FK 보유)
    private Bank bank;

    private Long userId;

    /**
     * 낙관적 락 버전 컬럼
     * - 동시에 두 트랜잭션이 같은 Account를 수정하려 할 때,
     * 먼저 커밋된 쪽이 version을 올리면 나머지는 ObjectOptimisticLockingFailureException 발생
     * - DB 컬럼: account.version (INT NOT NULL DEFAULT 0)
     */
    @Version
    @Column(nullable = false)
    private Long version;

    @Column(nullable = false, unique = true, length = 20)
    private String accountNumber;

    @Column(nullable = false, length = 50)
    private String ownerName;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AccountStatus status;


    // ── 생성 팩토리 메서드 ──────────────────────────────

    /**
     * @param accountNumber 한국 계좌번호 형식: 은행코드(3)+상품코드(2)+일련번호(6)+검증번호(1) = 12자리
     *                      예) 004-01-123456-7 (KB국민은행 개인입출금)
     */
    public static Account create(Bank bank, String accountNumber, String ownerName) {
        Account account = new Account();
        account.bank = bank;
        account.accountNumber = accountNumber;
        account.ownerName = ownerName;
        account.balance = BigDecimal.ZERO;
        account.status = AccountStatus.ACTIVE;
        return account;
    }

    // ── 도메인 비즈니스 메서드 ─────────────────────────

    /**
     * 입금
     */
    public AccountTransaction deposit(BigDecimal amount, String description) {
        validateActive();
        validatePositiveAmount(amount);

        this.balance = this.balance.add(amount);
        return AccountTransaction.ofDeposit(this, amount, this.balance, description);
    }

    /**
     * 출금
     */
    public AccountTransaction withdraw(BigDecimal amount, String description) {
        validateActive();
        validatePositiveAmount(amount);
        validateSufficientBalance(amount);

        this.balance = this.balance.subtract(amount);
        return AccountTransaction.ofWithdraw(this, amount, this.balance, description);
    }

    /**
     * 계좌 해지
     */
    public void close() {
        validateActive();
        if (this.balance.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("잔액이 남아 있는 계좌는 해지할 수 없습니다.");
        }
        this.status = AccountStatus.CLOSED;
    }

    // ── 검증 메서드 ────────────────────────────────────
    private void validateActive() {
        if (this.status != AccountStatus.ACTIVE) {
            throw new IllegalStateException("활성 상태의 계좌만 거래할 수 있습니다. 현재 상태: " + this.status);
        }
    }

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("거래 금액은 0보다 커야 합니다.");
        }
    }

    private void validateSufficientBalance(BigDecimal amount) {
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException(String.format("잔액이 부족합니다. 현재 잔액: %s, 요청 금액: %s", this.balance, amount));
        }
    }
}
