package com.revy.securities.domain.exchange.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.securities.domain.exchange.ExRate;
import com.revy.securities.domain.exchange.ExRateHistory;
import com.revy.securities.domain.exchange.ExchangeConfig;
import com.revy.securities.domain.exchange.QExRate;
import com.revy.securities.domain.exchange.QExchangeConfig;
import com.revy.securities.domain.exchange.repo.ExchangeRateConfigRepository;
import com.revy.securities.domain.exchange.repo.ExchangeRateHistoryRepository;
import com.revy.securities.domain.exchange.repo.ExchangeRateRepository;
import com.revy.securities.domain.exchange.service.ExchangeReteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
class ExchangeReteServiceImpl implements ExchangeReteService {
    private final ExchangeRateConfigRepository exchangeRateConfigRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final ExchangeRateHistoryRepository exchangeRateHistoryRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Transactional(readOnly = true)
    @Override
    public long getExchangeRateCount() {
        return exchangeRateRepository.count();
    }

    @Transactional(readOnly = true)
    @Override
    public long getExchangeRateConfigCount() {
        return exchangeRateConfigRepository.count();
    }


    @Transactional
    @Override
    public List<ExRate> saveExchangeRate(List<ExRate> exRates) {
        exRates = exchangeRateRepository.saveAll(exRates);
        exchangeRateHistoryRepository.saveAll(
                exRates.stream().map(ExRateHistory::createExchangeRateHistory).toList());
        return exRates;
    }

    @Transactional
    @Override
    public ExchangeConfig save(ExchangeConfig config) {
        return exchangeRateConfigRepository.save(config);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ExchangeConfig> findAllConfig() {
        return exchangeRateConfigRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public List<ExRate> getCurrentExchangeRate() {
        QExRate exRate = QExRate.exRate;
        QExchangeConfig exchangeConfig = QExchangeConfig.exchangeConfig;

        BooleanBuilder where = new BooleanBuilder();
        where.and(exchangeConfig.enabled.eq(true));
        return jpaQueryFactory.selectFrom(exRate)
                              .join(exchangeConfig)
                              .on(exRate.source.eq(exchangeConfig.source)
                                                           .and(exRate.dest.eq(exchangeConfig.dest)))
                              .where(where)
                              .orderBy(exRate.source.asc(), exRate.dest.asc())
                              .fetch();

    }

}
