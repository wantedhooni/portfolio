package com.example.querydslsqldemo.repository;

import com.example.querydslsqldemo.querydsl.QDailyProductSalesStat;
import com.example.querydslsqldemo.querydsl.QOrders;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQueryFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DailyProductSalesStatSqlRepository {

    private final SQLQueryFactory queryFactory;

    public long countOrders() {
        QOrders orders = QOrders.orders;

        Long count = queryFactory
                .select(orders.id.count())
                .from(orders)
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

    public long insertDailyStatByInsertSelect(LocalDate targetDate) {
        QOrders orders = QOrders.orders;
        QDailyProductSalesStat stat = QDailyProductSalesStat.dailyProductSalesStat;

        LocalDateTime from = targetDate.atStartOfDay();
        LocalDateTime to = targetDate.plusDays(1).atStartOfDay();

        return queryFactory
                .insert(stat)
                .columns(
                        stat.statDate,
                        stat.productId,
                        stat.totalAmount,
                        stat.orderCount
                )
                .select(
                        SQLExpressions
                                .select(
                                        Expressions.constant(targetDate),
                                        orders.productId,
                                        orders.amount.sum(),
                                        orders.id.count()
                                )
                                .from(orders)
                                .where(
                                        orders.orderedAt.goe(from),
                                        orders.orderedAt.lt(to),
                                        orders.status.eq("PAID")
                                )
                                .groupBy(orders.productId)
                )
                .execute();
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
                .select(
                        stat.statDate,
                        stat.productId,
                        stat.totalAmount,
                        stat.orderCount
                )
                .from(stat)
                .where(stat.statDate.eq(targetDate))
                .orderBy(stat.productId.asc())
                .limit(limit)
                .fetch()
                .stream()
                .map(this::toRow)
                .toList();
    }

    private DailyProductSalesStatRow toRow(Tuple tuple) {
        QDailyProductSalesStat stat = QDailyProductSalesStat.dailyProductSalesStat;

        return new DailyProductSalesStatRow(
                tuple.get(stat.statDate),
                tuple.get(stat.productId),
                tuple.get(stat.totalAmount),
                tuple.get(stat.orderCount)
        );
    }

    public record DailyProductSalesStatRow(
            LocalDate statDate,
            Long productId,
            BigDecimal totalAmount,
            Long orderCount
    ) {
    }
}
