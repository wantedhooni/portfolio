package com.revy.example.account.reader.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.example.account.reader.AccountReader;
import com.revy.example.account.reader.dto.AccountResult;
import com.revy.example.account.reader.dto.AccountSearchCondition;
import com.revy.example.account.reader.dto.AccountTxResult;
import com.revy.example.account.reader.dto.AccountTxSearchCondition;
import com.revy.example.account.reader.dto.StockPositionResult;
import com.revy.example.domain.account.Account;
import com.revy.example.domain.account.AccountTx;
import com.revy.example.domain.account.QAccount;
import com.revy.example.domain.account.QAccountTx;
import com.revy.example.domain.account.QPositionLot;
import com.revy.example.domain.account.QStockPosition;
import com.revy.example.domain.account.StockPosition;
import com.revy.example.utils.QuerydslUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AccountReaderImpl implements AccountReader {

    private final JPAQueryFactory jpaQueryFactory;

    private final QAccount       ACCOUNT      = QAccount.account;
    private final QAccountTx     ACCOUNT_TX   = QAccountTx.accountTx;
    private final QStockPosition POSITION     = QStockPosition.stockPosition;
    private final QPositionLot   LOT          = QPositionLot.positionLot;

    // ── Account ──────────────────────────────────────────────────

    @Override
    public Optional<AccountResult> getAccountById(Long id) {
        Account account = jpaQueryFactory.selectFrom(ACCOUNT).where(ACCOUNT.id.eq(id)).fetchOne();
        return Optional.ofNullable(account).map(AccountResult::from);
    }

    @Override
    public Optional<AccountResult> findByAccountNumber(String accountNumber) {
        Account account = jpaQueryFactory.selectFrom(ACCOUNT)
            .where(ACCOUNT.accountNumber.eq(accountNumber))
            .fetchOne();
        return Optional.ofNullable(account).map(AccountResult::from);
    }

    @Override
    public boolean existsByAccountNumber(String accountNumber) {
        return jpaQueryFactory.selectOne()
                              .from(ACCOUNT)
                              .where(ACCOUNT.accountNumber.eq(accountNumber))
                              .fetchFirst() != null;
    }

    @Override
    public Page<AccountResult> searchAccounts(Pageable pageable, AccountSearchCondition condition) {
        BooleanBuilder where = buildAccountCondition(condition);

        List<Account> content = jpaQueryFactory.selectFrom(ACCOUNT)
            .where(where)
            .orderBy(ACCOUNT.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory.select(ACCOUNT.count())
            .from(ACCOUNT)
            .where(where);

        return PageableExecutionUtils.getPage(
            content.stream().map(AccountResult::from).toList(),
            pageable,
            () -> Optional.ofNullable(countQuery.fetchOne()).orElse(0L)
        );
    }

    @Override
    public List<AccountResult> findAllByUserId(Long userId) {
        return jpaQueryFactory.selectFrom(ACCOUNT)
                              .where(ACCOUNT.userId.eq(userId))
                              .orderBy(ACCOUNT.id.asc())
                              .fetch().stream()
                              .map(AccountResult::from)
                              .toList();
    }

    private BooleanBuilder buildAccountCondition(AccountSearchCondition condition) {
        BooleanBuilder where = new BooleanBuilder();
        if (condition == null) return where;

        where.and(QuerydslUtils.eq(ACCOUNT.userId, condition.getUserId()));
        where.and(QuerydslUtils.eq(ACCOUNT.accountNumber, condition.getAccountNumber()));
        where.and(QuerydslUtils.eq(ACCOUNT.accountType, condition.getAccountType()));
        where.and(QuerydslUtils.eq(ACCOUNT.status, condition.getStatus()));
        where.and(QuerydslUtils.eq(ACCOUNT.currency, condition.getCurrency()));
        return where;
    }

    // ── AccountTx ────────────────────────────────────────────────

    @Override
    public Optional<AccountTxResult> getAccountTxById(Long id) {
        AccountTx tx = jpaQueryFactory.selectFrom(ACCOUNT_TX).where(ACCOUNT_TX.id.eq(id)).fetchOne();
        return Optional.ofNullable(tx).map(AccountTxResult::from);
    }

    @Override
    public boolean existsTxByReferenceId(String referenceId) {
        return jpaQueryFactory.selectOne()
                              .from(ACCOUNT_TX)
                              .where(ACCOUNT_TX.referenceId.eq(referenceId))
                              .fetchFirst() != null;
    }

    @Override
    public Page<AccountTxResult> searchAccountTx(Pageable pageable, AccountTxSearchCondition condition) {
        BooleanBuilder where = buildAccountTxCondition(condition);

        List<AccountTx> content = jpaQueryFactory.selectFrom(ACCOUNT_TX)
            .where(where)
            .orderBy(ACCOUNT_TX.tradedAt.desc(), ACCOUNT_TX.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory.select(ACCOUNT_TX.count())
            .from(ACCOUNT_TX)
            .where(where);

        return PageableExecutionUtils.getPage(
            content.stream().map(AccountTxResult::from).toList(),
            pageable,
            () -> Optional.ofNullable(countQuery.fetchOne()).orElse(0L)
        );
    }

    private BooleanBuilder buildAccountTxCondition(AccountTxSearchCondition condition) {
        BooleanBuilder where = new BooleanBuilder();
        if (condition == null) return where;

        where.and(QuerydslUtils.eq(ACCOUNT_TX.id, condition.getId()));
        where.and(QuerydslUtils.eq(ACCOUNT_TX.accountId, condition.getAccountId()));
        where.and(QuerydslUtils.eq(ACCOUNT_TX.txType, condition.getTxType()));
        where.and(QuerydslUtils.eq(ACCOUNT_TX.stockId, condition.getStockId()));
        where.and(QuerydslUtils.eq(ACCOUNT_TX.status, condition.getStatus()));
        where.and(QuerydslUtils.eq(ACCOUNT_TX.referenceId, condition.getReferenceId()));
        where.and(QuerydslUtils.goe(ACCOUNT_TX.tradedAt, condition.getTradedAtFrom()));
        where.and(QuerydslUtils.loe(ACCOUNT_TX.tradedAt, condition.getTradedAtTo()));
        return where;
    }

    // ── StockPosition ────────────────────────────────────────────

    @Override
    public Optional<StockPositionResult> findPositionByAccountAndStock(Long accountId, Long stockId) {
        StockPosition position = jpaQueryFactory.selectFrom(POSITION).distinct()
            .leftJoin(POSITION.lots, LOT).fetchJoin()
            .where(POSITION.accountId.eq(accountId).and(POSITION.stockId.eq(stockId)))
            .fetchOne();
        return Optional.ofNullable(position).map(StockPositionResult::from);
    }

    @Override
    public List<StockPositionResult> findAllPositionsByAccountId(Long accountId) {
        return jpaQueryFactory.selectFrom(POSITION).distinct()
                              .leftJoin(POSITION.lots, LOT).fetchJoin()
                              .where(POSITION.accountId.eq(accountId))
                              .orderBy(POSITION.id.asc())
                              .fetch().stream()
                              .map(StockPositionResult::from)
                              .toList();
    }
}
