package com.revy.example.ledger.reader.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.example.domain.ledger.AccountingPeriod;
import com.revy.example.domain.ledger.JournalEntry;
import com.revy.example.domain.ledger.LedgerAccount;
import com.revy.example.domain.ledger.QAccountingPeriod;
import com.revy.example.domain.ledger.QJournalEntry;
import com.revy.example.domain.ledger.QJournalLine;
import com.revy.example.domain.ledger.QLedgerAccount;
import com.revy.example.domain.ledger.enums.AccountCategory;
import com.revy.example.domain.ledger.enums.JournalStatus;
import com.revy.example.domain.ledger.enums.NormalBalance;
import com.revy.example.ledger.reader.LedgerReader;
import com.revy.example.ledger.reader.dto.AccountingPeriodResult;
import com.revy.example.ledger.reader.dto.JournalEntryResult;
import com.revy.example.ledger.reader.dto.JournalEntrySearchCondition;
import com.revy.example.ledger.reader.dto.LedgerAccountResult;
import com.revy.example.ledger.reader.dto.LedgerAccountSearchCondition;
import com.revy.example.ledger.reader.dto.TrialBalanceLine;
import com.revy.example.utils.QuerydslUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LedgerReaderImpl implements LedgerReader {

    private final JPAQueryFactory jpaQueryFactory;

    private final QLedgerAccount    ACC   = QLedgerAccount.ledgerAccount;
    private final QAccountingPeriod PER   = QAccountingPeriod.accountingPeriod;
    private final QJournalEntry     ENTRY = QJournalEntry.journalEntry;
    private final QJournalLine      LINE  = QJournalLine.journalLine;

    // ── LedgerAccount ────────────────────────────────────────────

    @Override
    public Optional<LedgerAccountResult> findAccountById(Long id) {
        LedgerAccount a = jpaQueryFactory.selectFrom(ACC).where(ACC.id.eq(id)).fetchOne();
        return Optional.ofNullable(a).map(LedgerAccountResult::from);
    }

    @Override
    public Optional<LedgerAccountResult> findAccountByCode(String accountCode) {
        LedgerAccount a = jpaQueryFactory.selectFrom(ACC).where(ACC.accountCode.eq(accountCode)).fetchOne();
        return Optional.ofNullable(a).map(LedgerAccountResult::from);
    }

    @Override
    public boolean existsAccountByCode(String accountCode) {
        return jpaQueryFactory.selectOne().from(ACC)
            .where(ACC.accountCode.eq(accountCode)).fetchFirst() != null;
    }

    @Override
    public Page<LedgerAccountResult> searchAccounts(Pageable pageable, LedgerAccountSearchCondition condition) {
        BooleanBuilder where = new BooleanBuilder();
        if (condition != null) {
            where.and(QuerydslUtils.eq(ACC.accountCode, condition.getAccountCode()));
            where.and(QuerydslUtils.like(ACC.name, condition.getName()));
            where.and(QuerydslUtils.eq(ACC.category, condition.getCategory()));
            where.and(QuerydslUtils.eq(ACC.parentId, condition.getParentId()));
            where.and(QuerydslUtils.eq(ACC.currency, condition.getCurrency()));
            if (condition.getIsActive() != null) where.and(ACC.isActive.eq(condition.getIsActive()));
        }

        List<LedgerAccount> content = jpaQueryFactory.selectFrom(ACC)
            .where(where).orderBy(ACC.accountCode.asc())
            .offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory.select(ACC.count()).from(ACC).where(where);

        return PageableExecutionUtils.getPage(
            content.stream().map(LedgerAccountResult::from).toList(),
            pageable, () -> Optional.ofNullable(countQuery.fetchOne()).orElse(0L)
        );
    }

    // ── AccountingPeriod ─────────────────────────────────────────

    @Override
    public Optional<AccountingPeriodResult> findPeriodById(Long id) {
        AccountingPeriod p = jpaQueryFactory.selectFrom(PER).where(PER.id.eq(id)).fetchOne();
        return Optional.ofNullable(p).map(AccountingPeriodResult::from);
    }

    @Override
    public Optional<AccountingPeriodResult> findPeriodByYearAndMonth(Integer fiscalYear, Integer fiscalPeriod) {
        AccountingPeriod p = jpaQueryFactory.selectFrom(PER)
            .where(PER.fiscalYear.eq(fiscalYear).and(PER.fiscalPeriod.eq(fiscalPeriod)))
            .fetchOne();
        return Optional.ofNullable(p).map(AccountingPeriodResult::from);
    }

    @Override
    public Optional<AccountingPeriodResult> findPeriodContaining(LocalDate date) {
        AccountingPeriod p = jpaQueryFactory.selectFrom(PER)
            .where(PER.startDate.loe(date).and(PER.endDate.goe(date)))
            .fetchOne();
        return Optional.ofNullable(p).map(AccountingPeriodResult::from);
    }

    // ── JournalEntry ─────────────────────────────────────────────

    @Override
    public Optional<JournalEntryResult> findEntryById(Long id) {
        JournalEntry e = jpaQueryFactory.selectFrom(ENTRY).distinct()
            .leftJoin(ENTRY.lines, LINE).fetchJoin()
            .where(ENTRY.id.eq(id)).fetchOne();
        return Optional.ofNullable(e).map(JournalEntryResult::from);
    }

    @Override
    public Optional<JournalEntryResult> findEntryByNumber(String journalNumber) {
        JournalEntry e = jpaQueryFactory.selectFrom(ENTRY).distinct()
            .leftJoin(ENTRY.lines, LINE).fetchJoin()
            .where(ENTRY.journalNumber.eq(journalNumber)).fetchOne();
        return Optional.ofNullable(e).map(JournalEntryResult::from);
    }

    @Override
    public Page<JournalEntryResult> searchEntries(Pageable pageable, JournalEntrySearchCondition condition) {
        BooleanBuilder where = new BooleanBuilder();
        if (condition != null) {
            where.and(QuerydslUtils.eq(ENTRY.journalNumber, condition.getJournalNumber()));
            where.and(QuerydslUtils.eq(ENTRY.periodId, condition.getPeriodId()));
            where.and(QuerydslUtils.eq(ENTRY.status, condition.getStatus()));
            where.and(QuerydslUtils.eq(ENTRY.referenceType, condition.getReferenceType()));
            where.and(QuerydslUtils.eq(ENTRY.referenceId, condition.getReferenceId()));
            if (condition.getEntryDateFrom() != null) where.and(ENTRY.entryDate.goe(condition.getEntryDateFrom()));
            if (condition.getEntryDateTo() != null)   where.and(ENTRY.entryDate.loe(condition.getEntryDateTo()));
        }

        List<JournalEntry> content = jpaQueryFactory.selectFrom(ENTRY)
            .where(where).orderBy(ENTRY.entryDate.desc(), ENTRY.id.desc())
            .offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory.select(ENTRY.count()).from(ENTRY).where(where);

        return PageableExecutionUtils.getPage(
            content.stream().map(JournalEntryResult::from).toList(),
            pageable, () -> Optional.ofNullable(countQuery.fetchOne()).orElse(0L)
        );
    }

    // ── Trial Balance ────────────────────────────────────────────

    @Override
    public List<TrialBalanceLine> trialBalance(LocalDate from, LocalDate to) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(ENTRY.status.eq(JournalStatus.POSTED));
        if (from != null) where.and(ENTRY.entryDate.goe(from));
        if (to != null)   where.and(ENTRY.entryDate.loe(to));

        List<Tuple> rows = jpaQueryFactory
            .select(ACC.id, ACC.accountCode, ACC.name, ACC.category, ACC.normalBalance,
                    LINE.debit.sum(), LINE.credit.sum())
            .from(LINE)
            .join(ENTRY).on(ENTRY.id.eq(LINE.journal.id))
            .join(ACC).on(ACC.id.eq(LINE.ledgerAccountId))
            .where(where)
            .groupBy(ACC.id, ACC.accountCode, ACC.name, ACC.category, ACC.normalBalance)
            .orderBy(ACC.accountCode.asc())
            .fetch();

        return rows.stream().map(t -> {
            BigDecimal d = Optional.ofNullable(t.get(LINE.debit.sum())).orElse(BigDecimal.ZERO);
            BigDecimal c = Optional.ofNullable(t.get(LINE.credit.sum())).orElse(BigDecimal.ZERO);
            NormalBalance nb = t.get(ACC.normalBalance);
            // 정상잔액 방향으로 balance 계산 (DEBIT 잔액 = debit - credit, CREDIT 잔액 = credit - debit)
            BigDecimal bal = nb == NormalBalance.DEBIT ? d.subtract(c) : c.subtract(d);
            return new TrialBalanceLine(
                t.get(ACC.id), t.get(ACC.accountCode), t.get(ACC.name),
                t.get(ACC.category), nb, d, c, bal
            );
        }).toList();
    }
}
