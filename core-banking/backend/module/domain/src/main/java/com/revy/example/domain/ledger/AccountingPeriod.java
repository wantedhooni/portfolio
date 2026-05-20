package com.revy.example.domain.ledger;

import com.revy.example.domain.common.BaseEntity;
import com.revy.example.domain.ledger.enums.PeriodStatus;
import com.revy.example.domain.ledger.exception.ClosedPeriodException;
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

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "accounting_period",
    uniqueConstraints = @UniqueConstraint(name = "uq_period_year_month", columnNames = {"fiscal_year", "fiscal_period"}),
    indexes = @Index(name = "idx_period_status", columnList = "status")
)
public class AccountingPeriod extends BaseEntity {

    @Column(name = "fiscal_year", nullable = false)
    private Integer fiscalYear;

    /** 회계 기간 (1~12 월) */
    @Column(name = "fiscal_period", nullable = false)
    private Integer fiscalPeriod;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PeriodStatus status;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "closed_by_admin_id")
    private Long closedByAdminId;

    // ── 팩토리 ────────────────────────────────────────────────────
    public static AccountingPeriod open(Integer fiscalYear, Integer fiscalPeriod,
                                        LocalDate startDate, LocalDate endDate) {
        AccountingPeriod p = new AccountingPeriod();
        p.fiscalYear   = fiscalYear;
        p.fiscalPeriod = fiscalPeriod;
        p.startDate    = startDate;
        p.endDate      = endDate;
        p.status       = PeriodStatus.OPEN;
        return p;
    }

    // ── 비즈니스 ──────────────────────────────────────────────────
    public void close(Long adminId, Instant now) {
        if (this.status == PeriodStatus.CLOSED) return;
        this.status           = PeriodStatus.CLOSED;
        this.closedAt         = now;
        this.closedByAdminId  = adminId;
    }

    public void validateOpen() {
        if (this.status == PeriodStatus.CLOSED) {
            throw new ClosedPeriodException();
        }
    }

    public boolean contains(LocalDate date) {
        return !date.isBefore(this.startDate) && !date.isAfter(this.endDate);
    }
}
