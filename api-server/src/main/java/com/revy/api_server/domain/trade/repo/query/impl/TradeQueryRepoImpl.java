package com.revy.api_server.domain.trade.repo.query.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.api_server.domain.trade.repo.query.TradeQueryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TradeQueryRepoImpl implements TradeQueryRepo {
    private final JPAQueryFactory jpaQueryFactory;
}
