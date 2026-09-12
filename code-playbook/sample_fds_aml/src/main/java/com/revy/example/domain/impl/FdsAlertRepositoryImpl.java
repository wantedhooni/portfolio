package com.revy.example.domain.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.example.domain.FdsAlert;
import com.revy.example.domain.FdsAlertRepositoryCustom;
import com.revy.example.domain.QFdsAlert;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FdsAlertRepositoryImpl implements FdsAlertRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QFdsAlert alert = QFdsAlert.fdsAlert;

    public Page<FdsAlert> searchAlerts(AlertSearchCondition condition, Pageable pageable) {

        BooleanBuilder builder = new BooleanBuilder();

        if (condition.getSeverity() != null) {
            builder.and(alert.severity.eq(condition.getSeverity()));
        }
        if (condition.getStatus() != null) {
            builder.and(alert.status.eq(condition.getStatus()));
        }
        if (condition.getRuleCode() != null) {
            builder.and(alert.ruleCode.eq(condition.getRuleCode()));
        }
        if (condition.getFromDate() != null) {
            builder.and(alert.createdAt.goe(condition.getFromDate().atStartOfDay()));
        }
        if (condition.getToDate() != null) {
            builder.and(alert.createdAt.loe(condition.getToDate().atTime(LocalTime.MAX)));
        }
        if (condition.getAccountId() != null) {
            builder.and(alert.accountId.eq(condition.getAccountId()));
        }

        List<FdsAlert> content = queryFactory
            .selectFrom(alert)
            .where(builder)
            .orderBy(alert.severity.desc(), alert.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(alert.count())
            .from(alert)
            .where(builder)
            .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    // 계좌별 30일 알림 집계
    public List<AlertStatDto> getAlertStatsByAccount(UUID accountId, int days) {
        LocalDateTime from = LocalDateTime.now().minusDays(days);

        return queryFactory
            .select(Projections.constructor(AlertStatDto.class,
                alert.ruleCode,
                alert.severity,
                alert.count(),
                alert.score.avg()
            ))
            .from(alert)
            .where(
                alert.accountId.eq(accountId),
                alert.createdAt.goe(from)
            )
            .groupBy(alert.ruleCode, alert.severity)
            .orderBy(alert.count().desc())
            .fetch();
    }
}