package com.revy.securities.domain.trade.repo.query.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.securities.domain.trade.repo.query.PositionQueryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PositionQueryRepoImpl implements PositionQueryRepo {
    private final JPAQueryFactory jpaQueryFactory;
}
