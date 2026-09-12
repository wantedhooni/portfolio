package com.revy.api_server.domain.trade.repo.query.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.api_server.domain.trade.repo.query.OrderFillQueryRepo;
import com.revy.api_server.domain.trade.repo.query.TradeQueryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderFillQueryRepoImpl implements OrderFillQueryRepo {
    private final JPAQueryFactory jpaQueryFactory;
}
