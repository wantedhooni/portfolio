package com.revy.example.domain.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.example.domain.QTransaction;
import com.revy.example.domain.Transaction;
import com.revy.example.domain.TransactionRepositoryCustom;
import com.revy.example.domain.TransactionSummary;
import com.revy.example.enums.TransactionStatus;
import com.revy.example.enums.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryImpl implements TransactionRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QTransaction txn = QTransaction.transaction;

    @Override
    public List<Transaction> findByAccountIdWithinPeriod(
        UUID accountId, LocalDateTime from, LocalDateTime to
                                                        ) {
        return queryFactory
            .selectFrom(txn)
            .where(
                txn.accountId.eq(accountId),
                txn.createdAt.between(from, to),
                txn.status.ne(TransactionStatus.CANCELLED)
                  )
            .orderBy(txn.createdAt.desc())
            .fetch();
    }

    @Override
    public BigDecimal sumAmountByAccountIdAndType(
        UUID accountId, TransactionType type, LocalDateTime from, LocalDateTime to
                                                 ) {
        return queryFactory
            .select(txn.amount.amount.sum())
            .from(txn)
            .where(
                txn.accountId.eq(accountId),
                txn.transactionType.eq(type),
                txn.createdAt.between(from, to),
                txn.status.eq(TransactionStatus.APPROVED)
                  )
            .fetchOne();
    }

    @Override
    public long countByAccountIdWithinHour(UUID accountId, LocalDateTime from) {
        Long count = queryFactory
            .select(txn.count())
            .from(txn)
            .where(
                txn.accountId.eq(accountId),
                txn.createdAt.goe(from),
                txn.status.ne(TransactionStatus.CANCELLED)
                  )
            .fetchOne();
        return count != null ? count : 0L;
    }

    /**
     * 분할 거래(Structuring) 의심 탐지:
     * 동일 계좌에서 당일 CTR 기준 이하로 쪼개서 여러 번 보내는 패턴
     */
    @Override
    public List<TransactionSummary> findPotentialStructuring(
        LocalDate targetDate, BigDecimal threshold
                                                            ) {
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);

        return queryFactory
            .select(Projections.constructor(TransactionSummary.class,
                                            txn.accountId,
                                            txn.count(),
                                            txn.amount.amount.sum()
                                           ))
            .from(txn)
            .where(
                txn.createdAt.between(startOfDay, endOfDay),
                txn.transactionType.in(TransactionType.WITHDRAWAL, TransactionType.TRANSFER),
                txn.status.eq(TransactionStatus.APPROVED)
                  )
            .groupBy(txn.accountId)
            .having(
                txn.count().goe(3L),                       // 3건 이상
                txn.amount.amount.sum().goe(threshold.multiply(BigDecimal.valueOf(0.7))),  // 기준의 70% 이상
                txn.amount.amount.max().lt(threshold)       // 개별 건은 기준 미만
                   )
            .fetch();
    }
}
