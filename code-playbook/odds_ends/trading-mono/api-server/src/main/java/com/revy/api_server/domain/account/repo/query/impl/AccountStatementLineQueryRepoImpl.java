package com.revy.api_server.domain.account.repo.query.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.api_server.domain.account.repo.query.AccountStatementLineQueryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AccountStatementLineQueryRepoImpl implements AccountStatementLineQueryRepo {
    private final JPAQueryFactory jpaQueryFactory;
}
