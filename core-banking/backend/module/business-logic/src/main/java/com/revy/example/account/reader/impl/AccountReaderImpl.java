package com.revy.example.account.reader.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.example.account.reader.AccountReader;
import com.revy.example.account.reader.dto.AccountTxSearchCondition;
import com.revy.example.domain.account.Account;
import com.revy.example.domain.account.AccountTx;
import com.revy.example.domain.account.QAccount;
import com.revy.example.domain.account.QAccountTx;
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

    private final QAccount ACCOUNT = QAccount.account;
    private final QAccountTx ACCOUNT_TX = QAccountTx.accountTx;

    @Override
    public Optional<Account> getAccountById(Long id) {
        return Optional.ofNullable(jpaQueryFactory.select(ACCOUNT)
                                       .from(ACCOUNT)
                                       .where(ACCOUNT.id.eq(id))
                                       .fetchOne());
    }

    @Override
    public Optional<AccountTx> getAccountTxById(Long id) {
        return Optional.ofNullable(jpaQueryFactory.select(ACCOUNT_TX)
                                       .from(ACCOUNT_TX)
                                       .where(ACCOUNT_TX.id.eq(id))
                                       .fetchOne());
    }

    @Override
    public Page<AccountTx> searchAccountTx(Pageable pageable, AccountTxSearchCondition condition) {
        BooleanBuilder where = makeAccountTxCondition(condition);
        List<AccountTx> content = jpaQueryFactory.selectFrom(ACCOUNT_TX)
            .where(where)
            .orderBy(ACCOUNT_TX.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
        JPAQuery<Long> countQuery = jpaQueryFactory.select(ACCOUNT_TX.count())
            .from(ACCOUNT_TX)
            .where(where);
        return PageableExecutionUtils.getPage(content, pageable, () -> Optional.ofNullable(countQuery.fetchOne())
            .orElse(0L));

    }

    private BooleanBuilder makeAccountTxCondition(AccountTxSearchCondition condition) {
        BooleanBuilder where = new BooleanBuilder();

        if (condition == null) {
            return where;
        }

        where.and(QuerydslUtils.eq(ACCOUNT_TX.id, condition.getId()));
        where.and(QuerydslUtils.eq(ACCOUNT_TX.accountId, condition.getAccountId()));
        where.and(QuerydslUtils.eq(ACCOUNT_TX.txType, condition.getTxType()));
        where.and(QuerydslUtils.eq(ACCOUNT_TX.stockId, condition.getStockId()));
        where.and(QuerydslUtils.eq(ACCOUNT_TX.status, condition.getStatus()));
        where.and(QuerydslUtils.eq(ACCOUNT_TX.referenceId, condition.getReferenceId()));
        return where;
    }


}
