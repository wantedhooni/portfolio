package com.revy.example.trade.reader.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.example.domain.account.AccountTx;
import com.revy.example.domain.account.QAccountTx;
import com.revy.example.domain.account.enums.TxType;
import com.revy.example.trade.reader.TradeReader;
import com.revy.example.trade.reader.dto.TradeResult;
import com.revy.example.trade.reader.dto.TradeSearchCondition;
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
public class TradeReaderImpl implements TradeReader {

    private final JPAQueryFactory jpaQueryFactory;

    private final QAccountTx TX = QAccountTx.accountTx;

    @Override
    public Optional<TradeResult> findById(Long id) {
        AccountTx tx = jpaQueryFactory.selectFrom(TX)
            .where(TX.id.eq(id).and(TX.txType.in(TxType.BUY, TxType.SELL)))
            .fetchOne();
        return Optional.ofNullable(tx).map(TradeResult::from);
    }

    @Override
    public Page<TradeResult> search(Pageable pageable, TradeSearchCondition condition) {
        BooleanBuilder where = buildWhere(condition);

        List<TradeResult> content = jpaQueryFactory.selectFrom(TX)
            .where(where)
            .orderBy(TX.tradedAt.desc(), TX.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch()
            .stream().map(TradeResult::from).toList();

        JPAQuery<Long> countQuery = jpaQueryFactory.select(TX.count()).from(TX).where(where);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanBuilder buildWhere(TradeSearchCondition c) {
        BooleanBuilder where = new BooleanBuilder();

        // 체결만 노출 — txType 미지정이면 BUY + SELL 전체
        if (c.getTxType() != null) {
            where.and(TX.txType.eq(c.getTxType()));
        } else {
            where.and(TX.txType.in(TxType.BUY, TxType.SELL));
        }

        if (c.getAccountId() != null) where.and(TX.accountId.eq(c.getAccountId()));
        if (c.getStockId()   != null) where.and(TX.stockId.eq(c.getStockId()));
        if (c.getStatus()    != null) where.and(TX.status.eq(c.getStatus()));
        if (c.getReferenceId() != null && !c.getReferenceId().isBlank())
            where.and(TX.referenceId.eq(c.getReferenceId()));
        if (c.getTradedAtFrom() != null) where.and(TX.tradedAt.goe(c.getTradedAtFrom()));
        if (c.getTradedAtTo()   != null) where.and(TX.tradedAt.loe(c.getTradedAtTo()));

        return where;
    }
}
