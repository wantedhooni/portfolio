package com.revy.securities.domain.trade.repo.query.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.securities.domain.trade.Order;
import com.revy.securities.domain.trade.QOrder;
import com.revy.securities.domain.trade.repo.query.OrderQueryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepoImpl implements OrderQueryRepo {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Order> findOrdersByUserId(Long ownerId, Pageable pageable) {
        QOrder order = QOrder.order;

        BooleanBuilder where = new BooleanBuilder();
        where.and(order.ownerId.eq(ownerId));

        List<Order> orders = jpaQueryFactory
                .selectFrom(order)
                .where(where)
                .orderBy(order.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        var countQuery = jpaQueryFactory
                .selectFrom(order)
                .where(order.ownerId.eq(ownerId));
        return PageableExecutionUtils.getPage(orders, pageable, countQuery::fetchCount);
    }
}
