package com.revy.example.stock.reader.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.example.domain.account.QStock;
import com.revy.example.domain.account.Stock;
import com.revy.example.stock.reader.StockReader;
import com.revy.example.stock.reader.dto.StockResult;
import com.revy.example.stock.reader.dto.StockSearchCondition;
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
public class StockReaderImpl implements StockReader {

    private final JPAQueryFactory jpaQueryFactory;

    private final QStock STOCK = QStock.stock;

    @Override
    public Optional<StockResult> findById(Long id) {
        Stock stock = jpaQueryFactory.selectFrom(STOCK).where(STOCK.id.eq(id)).fetchOne();
        return Optional.ofNullable(stock).map(StockResult::from);
    }

    @Override
    public Optional<StockResult> findByTickerAndExchange(String ticker, String exchange) {
        Stock stock = jpaQueryFactory.selectFrom(STOCK)
            .where(STOCK.ticker.eq(ticker).and(STOCK.exchange.eq(exchange)))
            .fetchOne();
        return Optional.ofNullable(stock).map(StockResult::from);
    }

    @Override
    public boolean existsByTickerAndExchange(String ticker, String exchange) {
        return jpaQueryFactory.selectOne()
                              .from(STOCK)
                              .where(STOCK.ticker.eq(ticker).and(STOCK.exchange.eq(exchange)))
                              .fetchFirst() != null;
    }

    @Override
    public Page<StockResult> search(Pageable pageable, StockSearchCondition condition) {
        BooleanBuilder where = buildCondition(condition);

        List<Stock> content = jpaQueryFactory.selectFrom(STOCK)
            .where(where)
            .orderBy(STOCK.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory.select(STOCK.count())
            .from(STOCK)
            .where(where);

        return PageableExecutionUtils.getPage(
            content.stream().map(StockResult::from).toList(),
            pageable,
            () -> Optional.ofNullable(countQuery.fetchOne()).orElse(0L)
        );
    }

    private BooleanBuilder buildCondition(StockSearchCondition condition) {
        BooleanBuilder where = new BooleanBuilder();
        if (condition == null) return where;

        where.and(QuerydslUtils.eq(STOCK.ticker, condition.getTicker()));
        where.and(QuerydslUtils.like(STOCK.name, condition.getName()));
        where.and(QuerydslUtils.eq(STOCK.exchange, condition.getExchange()));
        where.and(QuerydslUtils.eq(STOCK.sector, condition.getSector()));
        where.and(QuerydslUtils.eq(STOCK.currency, condition.getCurrency()));
        if (condition.getIsActive() != null) {
            where.and(STOCK.isActive.eq(condition.getIsActive()));
        }
        return where;
    }
}
