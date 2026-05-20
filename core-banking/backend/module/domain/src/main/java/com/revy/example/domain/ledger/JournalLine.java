package com.revy.example.domain.ledger;

import com.revy.example.domain.common.BaseEntity;
import com.revy.example.domain.ledger.exception.InvalidJournalLineException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "journal_line",
    indexes = {
        @Index(name = "idx_line_journal_id",       columnList = "journal_id"),
        @Index(name = "idx_line_ledger_account",   columnList = "ledger_account_id")
    }
)
public class JournalLine extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_id", nullable = false)
    private JournalEntry journal;

    @Column(name = "line_no", nullable = false)
    private Integer lineNo;

    @Column(name = "ledger_account_id", nullable = false)
    private Long ledgerAccountId;

    /** 차변 금액 — credit과 둘 중 하나만 양수, 다른 하나는 0 */
    @Column(name = "debit", nullable = false, precision = 20, scale = 4)
    private BigDecimal debit;

    /** 대변 금액 — debit과 둘 중 하나만 양수, 다른 하나는 0 */
    @Column(name = "credit", nullable = false, precision = 20, scale = 4)
    private BigDecimal credit;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "description", length = 500)
    private String description;

    // ── 팩토리 (package-private — JournalEntry를 통해서만 생성) ──
    static JournalLine of(JournalEntry journal, Integer lineNo, Long ledgerAccountId,
                          BigDecimal debit, BigDecimal credit, String currency, String description) {
        validateAmounts(debit, credit);
        JournalLine l = new JournalLine();
        l.journal          = journal;
        l.lineNo           = lineNo;
        l.ledgerAccountId  = ledgerAccountId;
        l.debit            = debit  == null ? BigDecimal.ZERO : debit;
        l.credit           = credit == null ? BigDecimal.ZERO : credit;
        l.currency         = currency;
        l.description      = description;
        return l;
    }

    /**
     * 차변·대변 중 정확히 한쪽만 양수, 다른 쪽은 0이어야 함.
     * 한 라인에 차변과 대변이 동시에 잡힐 수는 없음 (회계 원칙).
     */
    private static void validateAmounts(BigDecimal debit, BigDecimal credit) {
        BigDecimal d = debit  == null ? BigDecimal.ZERO : debit;
        BigDecimal c = credit == null ? BigDecimal.ZERO : credit;
        if (d.signum() < 0 || c.signum() < 0) {
            throw new InvalidJournalLineException(d, c);
        }
        boolean dPositive = d.signum() > 0;
        boolean cPositive = c.signum() > 0;
        if (dPositive == cPositive) {  // 둘 다 양수거나 둘 다 0 → 오류
            throw new InvalidJournalLineException(d, c);
        }
    }

    /** 차변인지 (debit > 0) */
    public boolean isDebit() {
        return this.debit.signum() > 0;
    }

    /** 금액의 절대값 (차변이든 대변이든) */
    public BigDecimal amount() {
        return isDebit() ? this.debit : this.credit;
    }
}
