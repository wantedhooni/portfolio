package com.revy.example.fx.reader.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.example.domain.fx.Currency;
import com.revy.example.domain.fx.ExchangeRate;
import com.revy.example.domain.fx.FxConversion;
import com.revy.example.domain.fx.QCurrency;
import com.revy.example.domain.fx.QExchangeRate;
import com.revy.example.domain.fx.QFxConversion;
import com.revy.example.domain.fx.enums.RateType;
import com.revy.example.fx.reader.FxReader;
import com.revy.example.fx.reader.dto.CurrencyResult;
import com.revy.example.fx.reader.dto.ExchangeRateResult;
import com.revy.example.fx.reader.dto.ExchangeRateSearchCondition;
import com.revy.example.fx.reader.dto.FxConversionResult;
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

    private final QCurrency     CUR  = QCurrency.currency;
    private final QExchangeRate RATE = QExchangeRate.exchangeRate;
    private final QFxConversion FX   = QFxConversion.fxConversion;

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

    @Override
    public Optional<ExchangeRateResult> findLatestRate(String baseCode, String quoteCode, RateType type) {
        ExchangeRate r = jpaQueryFactory.selectFrom(RATE)
            .where(RATE.baseCurrencyCode.eq(baseCode)
                .and(RATE.quoteCurrencyCode.eq(quoteCode))
                .and(RATE.rateType.eq(type)))
            .orderBy(RATE.quotedAt.desc())
            .limit(1)
            .fetchOne();
        return Optional.ofNullable(r).map(ExchangeRateResult::from);
    }

    @Override
    public Page<ExchangeRateResult> searchRates(Pageable pageable, ExchangeRateSearchCondition condition) {
        BooleanBuilder where = new BooleanBuilder();
        if (condition != null) {
            where.and(QuerydslUtils.eq(RATE.baseCurrencyCode, condition.getBaseCurrencyCode()));
            where.and(QuerydslUtils.eq(RATE.quoteCurrencyCode, condition.getQuoteCurrencyCode()));
            where.and(QuerydslUtils.eq(RATE.rateType, condition.getRateType()));
            where.and(QuerydslUtils.eq(RATE.source, condition.getSource()));
            where.and(QuerydslUtils.goe(RATE.quotedAt, condition.getQuotedFrom()));
            where.and(QuerydslUtils.loe(RATE.quotedAt, condition.getQuotedTo()));
        }

        List<ExchangeRate> content = jpaQueryFactory.selectFrom(RATE)
            .where(where).orderBy(RATE.quotedAt.desc(), RATE.id.desc())
            .offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory.select(RATE.count()).from(RATE).where(where);

        return PageableExecutionUtils.getPage(
            content.stream().map(ExchangeRateResult::from).toList(),
            pageable, () -> Optional.ofNullable(countQuery.fetchOne()).orElse(0L)
        );
    }

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
}
