package com.revy.example.domain.ledger;

import com.revy.example.domain.common.BaseEntity;
import com.revy.example.domain.ledger.enums.JournalStatus;
import com.revy.example.domain.ledger.exception.JournalAlreadyPostedException;
import com.revy.example.domain.ledger.exception.JournalNotPostedException;
import com.revy.example.domain.ledger.exception.UnbalancedJournalEntryException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "journal_entry",
    uniqueConstraints = @UniqueConstraint(name = "uq_journal_number", columnNames = "journal_number"),
    indexes = {
        @Index(name = "idx_journal_entry_date",    columnList = "entry_date"),
        @Index(name = "idx_journal_period_id",     columnList = "period_id"),
        @Index(name = "idx_journal_status",        columnList = "status"),
        @Index(name = "idx_journal_reference",     columnList = "reference_type,reference_id"),
        @Index(name = "idx_journal_posted_at",     columnList = "posted_at")
    }
)
public class JournalEntry extends BaseEntity {

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "journal_number", nullable = false, length = 30)
    private String journalNumber;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "period_id", nullable = false)
    private Long periodId;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    /** 원천 거래 유형 (ACCOUNT_TX / FX_CONVERSION / PREMIUM_PAYMENT / CLAIM_PAYOUT 등) */
    @Column(name = "reference_type", length = 30)
    private String referenceType;

    /** 원천 거래 ID */
    @Column(name = "reference_id", length = 64)
    private String referenceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private JournalStatus status;

    @Column(name = "posted_at")
    private Instant postedAt;

    /** 역분개 시 원본 분개 참조 */
    @Column(name = "reversed_by_journal_id")
    private Long reversedByJournalId;

    @Column(name = "reverses_journal_id")
    private Long reversesJournalId;

    @OneToMany(mappedBy = "journal", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("lineNo ASC")
    private List<JournalLine> lines = new ArrayList<>();

    // ── 팩토리 ────────────────────────────────────────────────────
    public static JournalEntry draft(String journalNumber, LocalDate entryDate, Long periodId,
                                     String description, String referenceType, String referenceId) {
        JournalEntry e = new JournalEntry();
        e.journalNumber  = journalNumber;
        e.entryDate      = entryDate;
        e.periodId       = periodId;
        e.description    = description;
        e.referenceType  = referenceType;
        e.referenceId    = referenceId;
        e.status         = JournalStatus.DRAFT;
        return e;
    }

    // ── 비즈니스 ──────────────────────────────────────────────────

    /** 차변 라인 추가 */
    public JournalLine addDebit(Long ledgerAccountId, BigDecimal amount, String currency, String description) {
        return addLine(ledgerAccountId, amount, BigDecimal.ZERO, currency, description);
    }

    /** 대변 라인 추가 */
    public JournalLine addCredit(Long ledgerAccountId, BigDecimal amount, String currency, String description) {
        return addLine(ledgerAccountId, BigDecimal.ZERO, amount, currency, description);
    }

    private JournalLine addLine(Long ledgerAccountId, BigDecimal debit, BigDecimal credit,
                                String currency, String description) {
        if (this.status != JournalStatus.DRAFT) {
            throw new JournalAlreadyPostedException();
        }
        int nextLineNo = this.lines.size() + 1;
        JournalLine line = JournalLine.of(this, nextLineNo, ledgerAccountId, debit, credit, currency, description);
        this.lines.add(line);
        return line;
    }

    /**
     * 전기 (장부 반영)
     * - Σ차변 = Σ대변 검증
     * - 최소 2개 라인 필요
     */
    public void post(Instant now) {
        if (this.status != JournalStatus.DRAFT) {
            throw new JournalAlreadyPostedException();
        }
        if (this.lines.size() < 2) {
            throw new UnbalancedJournalEntryException(totalDebit(), totalCredit());
        }
        BigDecimal d = totalDebit();
        BigDecimal c = totalCredit();
        if (d.compareTo(c) != 0) {
            throw new UnbalancedJournalEntryException(d, c);
        }
        this.status   = JournalStatus.POSTED;
        this.postedAt = now;
    }

    /**
     * 역분개 — 기존 라인의 차변/대변을 뒤집은 새 JournalEntry를 생성해서 반환.
     * 호출부에서 생성된 reversal을 persist + post 한 뒤 markReversedBy() 호출.
     */
    public JournalEntry createReversal(String newJournalNumber, LocalDate entryDate, Long periodId, String description) {
        if (this.status != JournalStatus.POSTED) {
            throw new JournalNotPostedException();
        }
        JournalEntry reversal = JournalEntry.draft(newJournalNumber, entryDate, periodId,
            "REVERSAL: " + description, this.referenceType, this.referenceId);
        reversal.reversesJournalId = this.getId();
        for (JournalLine line : this.lines) {
            reversal.addLine(line.getLedgerAccountId(),
                             line.getCredit(),   // 차변 <-> 대변
                             line.getDebit(),
                             line.getCurrency(),
                             "reverse: " + (line.getDescription() == null ? "" : line.getDescription()));
        }
        return reversal;
    }

    public void markReversedBy(Long reversalJournalId) {
        if (this.status != JournalStatus.POSTED) {
            throw new JournalNotPostedException();
        }
        this.reversedByJournalId = reversalJournalId;
        this.status              = JournalStatus.REVERSED;
    }

    public BigDecimal totalDebit() {
        return this.lines.stream()
                         .map(JournalLine::getDebit)
                         .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal totalCredit() {
        return this.lines.stream()
                         .map(JournalLine::getCredit)
                         .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
