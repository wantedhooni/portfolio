package com.revy.example.order.reader.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.example.domain.account.QStockOrder;
import com.revy.example.domain.account.StockOrder;
import com.revy.example.order.reader.OrderReader;
import com.revy.example.order.reader.dto.OrderResult;
import com.revy.example.order.reader.dto.OrderSearchCondition;
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
public class OrderReaderImpl implements OrderReader {

    private final JPAQueryFactory jpaQueryFactory;

    private final QStockOrder ORDER = QStockOrder.stockOrder;

    @Override
    public Optional<OrderResult> findById(Long id) {
        StockOrder o = jpaQueryFactory.selectFrom(ORDER).where(ORDER.id.eq(id)).fetchOne();
        return Optional.ofNullable(o).map(OrderResult::from);
    }

    @Override
    public Page<OrderResult> search(Pageable pageable, OrderSearchCondition c) {
        BooleanBuilder where = buildWhere(c);

        List<OrderResult> content = jpaQueryFactory.selectFrom(ORDER)
            .where(where)
            .orderBy(ORDER.orderedAt.desc(), ORDER.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch()
            .stream().map(OrderResult::from).toList();

        JPAQuery<Long> countQuery = jpaQueryFactory.select(ORDER.count()).from(ORDER).where(where);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanBuilder buildWhere(OrderSearchCondition c) {
        BooleanBuilder where = new BooleanBuilder();
        if (c.getAccountId()   != null) where.and(ORDER.accountId.eq(c.getAccountId()));
        if (c.getStockId()     != null) where.and(ORDER.stockId.eq(c.getStockId()));
        if (c.getSide()        != null) where.and(ORDER.side.eq(c.getSide()));
        if (c.getOrderType()   != null) where.and(ORDER.orderType.eq(c.getOrderType()));
        if (c.getStatus()      != null) where.and(ORDER.status.eq(c.getStatus()));
        if (c.getReferenceId() != null && !c.getReferenceId().isBlank())
            where.and(ORDER.referenceId.eq(c.getReferenceId()));
        if (c.getOrderedAtFrom() != null) where.and(ORDER.orderedAt.goe(c.getOrderedAtFrom()));
        if (c.getOrderedAtTo()   != null) where.and(ORDER.orderedAt.loe(c.getOrderedAtTo()));
        return where;
    }
}
