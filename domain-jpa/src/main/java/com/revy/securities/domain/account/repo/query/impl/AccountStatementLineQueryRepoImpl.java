package com.revy.securities.domain.account.repo.query.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.securities.domain.account.repo.query.AccountStatementLineQueryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AccountStatementLineQueryRepoImpl implements AccountStatementLineQueryRepo {
    private final JPAQueryFactory jpaQueryFactory;
}
