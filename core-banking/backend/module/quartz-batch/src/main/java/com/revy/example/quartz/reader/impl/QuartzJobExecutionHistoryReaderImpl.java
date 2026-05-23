package com.revy.example.quartz.reader.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.example.quartz.domain.QQuartzJobExecutionHistory;
import com.revy.example.quartz.domain.QuartzJobExecutionHistory;
import com.revy.example.quartz.dto.QuartzJobExecutionHistoryResult;
import com.revy.example.quartz.enums.QuartzJobExecutionStatus;
import com.revy.example.quartz.reader.QuartzJobExecutionHistoryReader;
import com.revy.example.utils.QuerydslUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * QueryDSL 기반으로 Quartz 작업 실행 이력을 조회하는 Reader 구현체이다.
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuartzJobExecutionHistoryReaderImpl implements QuartzJobExecutionHistoryReader {

    private final JPAQueryFactory jpaQueryFactory;
    private final QQuartzJobExecutionHistory history = QQuartzJobExecutionHistory.quartzJobExecutionHistory;

    /**
     * 실행 중인 작업의 상태 변경을 위해 fire instance id로 실행 이력 엔티티를 조회한다.
     */
    @Override
    public Optional<QuartzJobExecutionHistory> findByFireInstanceId(String fireInstanceId) {
        return Optional.ofNullable(jpaQueryFactory.selectFrom(history)
                                       .where(history.fireInstanceId.eq(fireInstanceId))
                                       .fetchOne());
    }

    @Override
    public Optional<QuartzJobExecutionHistory> findById(Long id) {
        return Optional.ofNullable(jpaQueryFactory.selectFrom(history)
                                       .where(history.id.eq(id))
                                       .fetchOne());
    }

    /**
     * 작업 그룹과 작업명에 해당하는 실행 이력을 최신 실행 순서로 projection 조회한다.
     */
    @Override
    public Page<QuartzJobExecutionHistoryResult> findByJob(String jobGroup, String jobName, Pageable pageable) {
        BooleanBuilder where = new BooleanBuilder();
        QuerydslUtils.eq(history.jobGroup, jobGroup);
        QuerydslUtils.eq(history.jobName, jobName);

        List<QuartzJobExecutionHistoryResult> content = jpaQueryFactory.select(historyProjection())
            .from(history)
            .where(where)
            .orderBy(history.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory.select(history.count())
            .from(history)
            .where(where);

        return PageableExecutionUtils.getPage(content, pageable, () -> Optional.ofNullable(countQuery.fetchOne())
            .orElse(0L));
    }

    /**
     * 실행 상태에 해당하는 실행 이력을 최신 실행 순서로 projection 조회한다.
     */
    @Override
    public Page<QuartzJobExecutionHistoryResult> findByStatus(QuartzJobExecutionStatus status, Pageable pageable) {
        BooleanBuilder where = new BooleanBuilder();
        QuerydslUtils.eq(history.status, status);

        List<QuartzJobExecutionHistoryResult> content = jpaQueryFactory.select(historyProjection())
            .from(history)
            .where(where)
            .orderBy(history.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory.select(history.count())
            .from(history)
            .where(where);

        return PageableExecutionUtils.getPage(content, pageable, () -> Optional.ofNullable(countQuery.fetchOne())
            .orElse(0L));
    }

    private ConstructorExpression<QuartzJobExecutionHistoryResult> historyProjection() {
        return Projections.constructor(QuartzJobExecutionHistoryResult.class, history.id, history.schedulerName,
                                       history.fireInstanceId, history.jobName, history.jobGroup, history.triggerName,
                                       history.triggerGroup, history.status, history.scheduledFireTime,
                                       history.fireTime, history.endTime, history.durationMs, history.errorMessage);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim()
            .isEmpty();
    }
}
