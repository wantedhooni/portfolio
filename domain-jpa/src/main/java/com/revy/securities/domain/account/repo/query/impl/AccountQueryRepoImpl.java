package com.revy.securities.domain.account.repo.query.impl;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.securities.domain.account.Account;
import com.revy.securities.domain.account.QAccount;
import com.revy.securities.domain.account.repo.query.AccountQueryRepo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AccountQueryRepoImpl implements AccountQueryRepo {

    private final JPAQueryFactory jpaQueryFactory;

    @PersistenceContext
    private final EntityManager em;
    @Override
    @Transactional(readOnly = true)
    public Optional<Account> findOneByAccountNo(String accountNo){
        var query = findQuery(null, accountNo);
        return Optional.ofNullable(query.fetchFirst());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Account> findOneByOwnerIdAndAccountNo(Long ownerId, String accountNo){
        var query = findQuery(ownerId, accountNo);
        return Optional.ofNullable(query.fetchFirst());
    }

    @Transactional
    @Override
    public long nextAccountNoSeq() {
        Number n = (Number) em.createNativeQuery("SELECT NEXT VALUE FOR account_no_seq").getSingleResult();
        return n.longValue();
    }

    private JPAQuery<Account> findQuery(Long ownerId, String accountNo) {
        var query = jpaQueryFactory.selectFrom(QAccount.account);
        if(ownerId != null){
            query.where(QAccount.account.ownerId.eq(ownerId));
        }

        if(accountNo != null){
            query.where(QAccount.account.accountNo.eq(accountNo));
        }
        // 정렬
        query.orderBy(QAccount.account.accountNo.asc());
        return query;
    }
}
