package com.revy.securities.domain.trade.repo.query.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.securities.domain.trade.repo.query.OrderFillQueryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderFillQueryRepoImpl implements OrderFillQueryRepo {
    private final JPAQueryFactory jpaQueryFactory;
}
