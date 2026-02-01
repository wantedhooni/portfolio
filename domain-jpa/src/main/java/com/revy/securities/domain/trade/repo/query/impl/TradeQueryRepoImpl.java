package com.revy.securities.domain.trade.repo.query.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.securities.domain.trade.repo.query.TradeQueryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TradeQueryRepoImpl implements TradeQueryRepo {
    private final JPAQueryFactory jpaQueryFactory;
}
