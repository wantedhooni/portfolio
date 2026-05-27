package com.revy.example.fx.reader.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.example.domain.fx.Currency;
import com.revy.example.domain.fx.ExchangeRate;
import com.revy.example.domain.fx.ExchangeRateHistory;
import com.revy.example.domain.fx.FxConversion;
import com.revy.example.domain.fx.FxCorridor;
import com.revy.example.domain.fx.QCurrency;
import com.revy.example.domain.fx.QExchangeRate;
import com.revy.example.domain.fx.QExchangeRateHistory;
import com.revy.example.domain.fx.QFxConversion;
import com.revy.example.domain.fx.QFxCorridor;
import com.revy.example.domain.fx.enums.CorridorStatus;
import com.revy.example.domain.fx.enums.RateType;
import com.revy.example.fx.reader.FxReader;
import com.revy.example.fx.reader.dto.CurrencyResult;
import com.revy.example.fx.reader.dto.ExchangeRateResult;
import com.revy.example.fx.reader.dto.ExchangeRateSearchCondition;
import com.revy.example.fx.reader.dto.FxConversionResult;
import com.revy.example.fx.reader.dto.FxCorridorResult;
import com.revy.example.fx.reader.dto.FxCorridorSearchCondition;
import com.revy.example.utils.QuerydslUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FxReaderImpl implements FxReader {

    private final JPAQueryFactory jpaQueryFactory;

    private final QCurrency            CUR      = QCurrency.currency;
    private final QExchangeRate        RATE     = QExchangeRate.exchangeRate;
    private final QExchangeRateHistory HIST     = QExchangeRateHistory.exchangeRateHistory;
    private final QFxConversion        FX       = QFxConversion.fxConversion;
    private final QFxCorridor         CORRIDOR = QFxCorridor.fxCorridor;

    // ── Currency ─────────────────────────────────────────────────

    @Override
    public Optional<CurrencyResult> findCurrencyByCode(String code) {
        Currency c = jpaQueryFactory.selectFrom(CUR).where(CUR.code.eq(code)).fetchOne();
        return Optional.ofNullable(c).map(CurrencyResult::from);
    }

    @Override
    public List<CurrencyResult> findAllActiveCurrencies() {
        return jpaQueryFactory.selectFrom(CUR).where(CUR.isActive.isTrue())
            .orderBy(CUR.code.asc()).fetch()
            .stream().map(CurrencyResult::from).toList();
    }

    @Override
    public boolean existsCurrencyByCode(String code) {
        return jpaQueryFactory.selectOne().from(CUR).where(CUR.code.eq(code)).fetchFirst() != null;
    }

    // ── ExchangeRate (현재 환율) ──────────────────────────────────

    @Override
    public Optional<ExchangeRateResult> findCurrentRate(String baseCode, String quoteCode, RateType type) {
        ExchangeRate r = jpaQueryFactory.selectFrom(RATE)
            .where(RATE.baseCurrencyCode.eq(baseCode)
                .and(RATE.quoteCurrencyCode.eq(quoteCode))
                .and(RATE.rateType.eq(type)))
            .fetchOne();
        return Optional.ofNullable(r).map(ExchangeRateResult::from);
    }

    @Override
    public List<ExchangeRateResult> findAllCurrentRates() {
        return jpaQueryFactory.selectFrom(RATE)
            .orderBy(RATE.baseCurrencyCode.asc(), RATE.quoteCurrencyCode.asc())
            .fetch()
            .stream().map(ExchangeRateResult::from).toList();
    }

    // ── ExchangeRateHistory (이력) ────────────────────────────────

    @Override
    public Page<ExchangeRateResult> searchRateHistory(Pageable pageable, ExchangeRateSearchCondition condition) {
        BooleanBuilder where = new BooleanBuilder();
        if (condition != null) {
            where.and(QuerydslUtils.eq(HIST.baseCurrencyCode,  condition.getBaseCurrencyCode()));
            where.and(QuerydslUtils.eq(HIST.quoteCurrencyCode, condition.getQuoteCurrencyCode()));
            where.and(QuerydslUtils.eq(HIST.rateType,          condition.getRateType()));
            where.and(QuerydslUtils.eq(HIST.source,            condition.getSource()));
            where.and(QuerydslUtils.goe(HIST.quotedAt,         condition.getQuotedFrom()));
            where.and(QuerydslUtils.loe(HIST.quotedAt,         condition.getQuotedTo()));
        }

        List<ExchangeRateHistory> content = jpaQueryFactory.selectFrom(HIST)
            .where(where)
            .orderBy(HIST.quotedAt.desc(), HIST.id.desc())
            .offset(pageable.getOffset()).limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory.select(HIST.count()).from(HIST).where(where);

        return PageableExecutionUtils.getPage(
            content.stream().map(ExchangeRateResult::fromHistory).toList(),
            pageable, () -> Optional.ofNullable(countQuery.fetchOne()).orElse(0L)
        );
    }

    // ── FxConversion ─────────────────────────────────────────────

    @Override
    public Optional<FxConversionResult> findConversionById(Long id) {
        FxConversion c = jpaQueryFactory.selectFrom(FX).where(FX.id.eq(id)).fetchOne();
        return Optional.ofNullable(c).map(FxConversionResult::from);
    }

    @Override
    public Optional<FxConversionResult> findConversionByNumber(String conversionNumber) {
        FxConversion c = jpaQueryFactory.selectFrom(FX)
            .where(FX.conversionNumber.eq(conversionNumber)).fetchOne();
        return Optional.ofNullable(c).map(FxConversionResult::from);
    }

    @Override
    public boolean existsConversionByReferenceId(String referenceId) {
        return jpaQueryFactory.selectOne().from(FX)
            .where(FX.referenceId.eq(referenceId)).fetchFirst() != null;
    }

    // ── FxCorridor ───────────────────────────────────────────────

    @Override
    public Optional<FxCorridorResult> findCorridorById(Long id) {
        FxCorridor c = jpaQueryFactory.selectFrom(CORRIDOR).where(CORRIDOR.id.eq(id)).fetchOne();
        return Optional.ofNullable(c).map(FxCorridorResult::from);
    }

    @Override
    public Optional<FxCorridorResult> findCorridorByPair(String baseCode, String quoteCode) {
        FxCorridor c = jpaQueryFactory.selectFrom(CORRIDOR)
            .where(CORRIDOR.baseCurrencyCode.eq(baseCode)
                .and(CORRIDOR.quoteCurrencyCode.eq(quoteCode)))
            .fetchOne();
        return Optional.ofNullable(c).map(FxCorridorResult::from);
    }

    @Override
    public List<FxCorridorResult> findAllActiveCorridors() {
        return jpaQueryFactory.selectFrom(CORRIDOR)
            .where(CORRIDOR.status.eq(CorridorStatus.ACTIVE))
            .orderBy(CORRIDOR.baseCurrencyCode.asc(), CORRIDOR.quoteCurrencyCode.asc())
            .fetch()
            .stream().map(FxCorridorResult::from).toList();
    }

    @Override
    public Page<FxCorridorResult> searchCorridors(Pageable pageable, FxCorridorSearchCondition condition) {
        BooleanBuilder where = new BooleanBuilder();
        if (condition != null) {
            where.and(QuerydslUtils.eq(CORRIDOR.baseCurrencyCode,  condition.getBaseCurrencyCode()));
            where.and(QuerydslUtils.eq(CORRIDOR.quoteCurrencyCode, condition.getQuoteCurrencyCode()));
            where.and(QuerydslUtils.eq(CORRIDOR.status,            condition.getStatus()));
        }

        List<FxCorridor> content = jpaQueryFactory.selectFrom(CORRIDOR)
            .where(where)
            .orderBy(CORRIDOR.baseCurrencyCode.asc(), CORRIDOR.quoteCurrencyCode.asc())
            .offset(pageable.getOffset()).limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory.select(CORRIDOR.count()).from(CORRIDOR).where(where);

        return PageableExecutionUtils.getPage(
            content.stream().map(FxCorridorResult::from).toList(),
            pageable, () -> Optional.ofNullable(countQuery.fetchOne()).orElse(0L)
        );
    }

    @Override
    public boolean existsCorridorByPair(String baseCode, String quoteCode) {
        return jpaQueryFactory.selectOne().from(CORRIDOR)
            .where(CORRIDOR.baseCurrencyCode.eq(baseCode)
                .and(CORRIDOR.quoteCurrencyCode.eq(quoteCode)))
            .fetchFirst() != null;
    }
}
