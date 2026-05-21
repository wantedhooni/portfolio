package com.revy.example.settlement.reader.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.example.domain.billing.QSettlement;
import com.revy.example.domain.billing.Settlement;
import com.revy.example.settlement.reader.SettlementReader;
import com.revy.example.settlement.reader.dto.SettlementResult;
import com.revy.example.settlement.reader.dto.SettlementSearchCondition;
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
public class SettlementReaderImpl implements SettlementReader {

    private final JPAQueryFactory jpaQueryFactory;
    private final QSettlement S = QSettlement.settlement;

    @Override
    public Optional<SettlementResult> findById(Long id) {
        Settlement s = jpaQueryFactory.selectFrom(S).where(S.id.eq(id)).fetchOne();
        return Optional.ofNullable(s).map(SettlementResult::from);
    }

    @Override
    public Page<SettlementResult> search(Pageable pageable, SettlementSearchCondition c) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(QuerydslUtils.eq(S.accountId, c.accountId()));
        where.and(QuerydslUtils.eq(S.type, c.type()));
        where.and(QuerydslUtils.eq(S.status, c.status()));
        where.and(QuerydslUtils.goe(S.settlementDate, c.settlementDateFrom()));
        where.and(QuerydslUtils.loe(S.settlementDate, c.settlementDateTo()));

        List<Settlement> content = jpaQueryFactory.selectFrom(S)
                .where(where)
                .orderBy(S.settlementDate.desc(), S.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory.select(S.count()).from(S).where(where);

        return PageableExecutionUtils.getPage(
                content.stream().map(SettlementResult::from).toList(),
                pageable,
                () -> Optional.ofNullable(countQuery.fetchOne()).orElse(0L)
        );
    }
}
