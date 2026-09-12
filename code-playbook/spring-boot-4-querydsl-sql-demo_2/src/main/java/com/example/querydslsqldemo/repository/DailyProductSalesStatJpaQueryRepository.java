package com.example.querydslsqldemo.repository;

import com.example.querydslsqldemo.entity.DailyProductSalesStat;
import com.example.querydslsqldemo.entity.QDailyProductSalesStat;
import com.example.querydslsqldemo.entity.QOrderEntity;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DailyProductSalesStatJpaQueryRepository {

    private static final int PERSIST_BATCH_SIZE = 1_000;

    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    public long countOrders() {
        QOrderEntity order = QOrderEntity.orderEntity;

        Long count = queryFactory
                .select(order.id.count())
                .from(order)
                .fetchOne();

        return count == null ? 0L : count;
    }

    public long deleteDailyStat(LocalDate targetDate) {
        QDailyProductSalesStat stat = QDailyProductSalesStat.dailyProductSalesStat;

        return queryFactory
                .delete(stat)
                .where(stat.statDate.eq(targetDate))
                .execute();
    }

    public long createDailyStatByQuerydslJpa(LocalDate targetDate) {
        List<DailyProductSalesStat> stats = aggregateDailyStats(targetDate)
                .stream()
                .map(DailyProductSalesStat::from)
                .toList();

        persistAll(stats);
        return stats.size();
    }

    public List<DailyProductSalesStatRow> aggregateDailyStats(LocalDate targetDate) {
        QOrderEntity order = QOrderEntity.orderEntity;

        LocalDateTime from = targetDate.atStartOfDay();
        LocalDateTime to = targetDate.plusDays(1).atStartOfDay();

        return queryFactory
                .select(Projections.constructor(
                        DailyProductSalesStatRow.class,
                        Expressions.constant(targetDate),
                        order.productId,
                        order.amount.sum(),
                        order.id.count()
                ))
                .from(order)
                .where(
                        order.orderedAt.goe(from),
                        order.orderedAt.lt(to),
                        order.status.eq("PAID")
                )
                .groupBy(order.productId)
                .fetch();
    }

    public long countDailyStat(LocalDate targetDate) {
        QDailyProductSalesStat stat = QDailyProductSalesStat.dailyProductSalesStat;

        Long count = queryFactory
                .select(stat.id.count())
                .from(stat)
                .where(stat.statDate.eq(targetDate))
                .fetchOne();

        return count == null ? 0L : count;
    }

    public List<DailyProductSalesStatRow> findDailyStats(LocalDate targetDate, long limit) {
        QDailyProductSalesStat stat = QDailyProductSalesStat.dailyProductSalesStat;

        return queryFactory
                .select(Projections.constructor(
                        DailyProductSalesStatRow.class,
                        stat.statDate,
                        stat.productId,
                        stat.totalAmount,
                        stat.orderCount
                ))
                .from(stat)
                .where(stat.statDate.eq(targetDate))
                .orderBy(stat.productId.asc())
                .limit(limit)
                .fetch();
    }

    private void persistAll(List<DailyProductSalesStat> stats) {
        for (int i = 0; i < stats.size(); i++) {
            entityManager.persist(stats.get(i));

            if ((i + 1) % PERSIST_BATCH_SIZE == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }

        entityManager.flush();
        entityManager.clear();
    }

    public record DailyProductSalesStatRow(
            LocalDate statDate,
            Long productId,
            BigDecimal totalAmount,
            Long orderCount
    ) {
    }
}
