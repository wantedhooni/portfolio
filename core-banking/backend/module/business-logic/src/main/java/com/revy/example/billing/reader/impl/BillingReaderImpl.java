package com.revy.example.billing.reader.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.example.billing.reader.BillingReader;
import com.revy.example.billing.reader.dto.BillingInvoiceResult;
import com.revy.example.billing.reader.dto.BillingSearchCondition;
import com.revy.example.domain.billing.BillingInvoice;
import com.revy.example.domain.billing.QBillingInvoice;
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
public class BillingReaderImpl implements BillingReader {

    private final JPAQueryFactory jpaQueryFactory;
    private final QBillingInvoice INV = QBillingInvoice.billingInvoice;

    @Override
    public Optional<BillingInvoiceResult> findById(Long id) {
        BillingInvoice inv = jpaQueryFactory.selectFrom(INV)
                .leftJoin(INV.items).fetchJoin()
                .where(INV.id.eq(id))
                .distinct()
                .fetchOne();
        return Optional.ofNullable(inv).map(BillingInvoiceResult::from);
    }

    @Override
    public Page<BillingInvoiceResult> search(Pageable pageable, BillingSearchCondition c) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(QuerydslUtils.eq(INV.accountId, c.accountId()));
        where.and(QuerydslUtils.like(INV.billingPeriod, c.billingPeriod()));
        where.and(QuerydslUtils.eq(INV.status, c.status()));

        List<BillingInvoice> content = jpaQueryFactory.selectFrom(INV)
                .where(where)
                .orderBy(INV.billingPeriod.desc(), INV.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory.select(INV.count()).from(INV).where(where);

        return PageableExecutionUtils.getPage(
                content.stream().map(BillingInvoiceResult::from).toList(),
                pageable,
                () -> Optional.ofNullable(countQuery.fetchOne()).orElse(0L)
        );
    }
}
