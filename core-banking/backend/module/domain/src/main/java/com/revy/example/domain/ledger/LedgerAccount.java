package com.revy.example.domain.ledger;

import com.revy.example.domain.common.BaseEntity;
import com.revy.example.domain.ledger.enums.AccountCategory;
import com.revy.example.domain.ledger.enums.NormalBalance;
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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "ledger_account",
    uniqueConstraints = @UniqueConstraint(name = "uq_ledger_account_code", columnNames = "account_code"),
    indexes = {
        @Index(name = "idx_ledger_category",  columnList = "category"),
        @Index(name = "idx_ledger_parent_id", columnList = "parent_id"),
        @Index(name = "idx_ledger_active",    columnList = "is_active")
    }
)
public class LedgerAccount extends BaseEntity {

    /** 계정 코드 (예: '1010', '1010.01', '2100.20') — 계층 표현 가능 */
    @Column(name = "account_code", nullable = false, length = 30)
    private String accountCode;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private AccountCategory category;

    /** category에서 파생 — 조회 효율을 위해 저장 */
    @Enumerated(EnumType.STRING)
    @Column(name = "normal_balance", nullable = false, length = 10)
    private NormalBalance normalBalance;

    /** 상위 계정 (계층 구조) */
    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "description", length = 500)
    private String description;

    // ── 팩토리 ────────────────────────────────────────────────────
    public static LedgerAccount create(String accountCode, String name, AccountCategory category,
                                       String currency, Long parentId, String description) {
        LedgerAccount a = new LedgerAccount();
        a.accountCode    = accountCode;
        a.name           = name;
        a.category       = category;
        a.normalBalance  = category.getNormalBalance();
        a.currency       = currency;
        a.parentId       = parentId;
        a.description    = description;
        a.isActive       = true;
        return a;
    }

    // ── 비즈니스 ──────────────────────────────────────────────────
    public void discontinue() {
        this.isActive = false;
    }

    public void rename(String name, String description) {
        this.name        = name;
        this.description = description;
    }
}
