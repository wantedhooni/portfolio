package com.revy.example.domain.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.example.domain.PurchaseOrder;
import com.revy.example.domain.PurchaseOrderQueryRepository;
import com.revy.example.domain.QPurchaseOrder;
import org.springframework.stereotype.Repository;

import java.util.List;



@Repository
public class PurchaseOrderQueryRepositoryImpl implements PurchaseOrderQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final QPurchaseOrder purchaseOrder = QPurchaseOrder.purchaseOrder;

    public PurchaseOrderQueryRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<PurchaseOrder> findByCustomerId(Long customerId) {
        return queryFactory
                .selectFrom(purchaseOrder)
                .where(purchaseOrder.customerId.eq(customerId))
                .orderBy(purchaseOrder.id.desc())
                .fetch();
    }
}