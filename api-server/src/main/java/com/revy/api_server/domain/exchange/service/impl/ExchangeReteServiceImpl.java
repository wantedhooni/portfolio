package com.revy.api_server.domain.exchange.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.api_server.domain.exchange.ExchangeRate;
import com.revy.api_server.domain.exchange.ExchangeRateConfig;
import com.revy.api_server.domain.exchange.ExchangeRateHistory;
import com.revy.api_server.domain.exchange.QExchangeRate;
import com.revy.api_server.domain.exchange.QExchangeRateConfig;
import com.revy.api_server.domain.exchange.repo.ExchangeRateConfigRepository;
import com.revy.api_server.domain.exchange.repo.ExchangeRateHistoryRepository;
import com.revy.api_server.domain.exchange.repo.ExchangeRateRepository;
import com.revy.api_server.domain.exchange.service.ExchangeReteService;
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
    public List<ExchangeRate> saveExchangeRate(List<ExchangeRate> exchangeRates) {
        exchangeRates = exchangeRateRepository.saveAll(exchangeRates);
        exchangeRateHistoryRepository.saveAll(
                exchangeRates.stream().map(ExchangeRateHistory::createExchangeRateHistory).toList());
        return exchangeRates;
    }

    @Transactional
    @Override
    public ExchangeRateConfig save(ExchangeRateConfig config) {
        return exchangeRateConfigRepository.save(config);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ExchangeRateConfig> findAllConfig() {
        return exchangeRateConfigRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public List<ExchangeRate> getCurrentExchangeRate() {
        QExchangeRate exchangeRate = QExchangeRate.exchangeRate;
        QExchangeRateConfig exchangeRateConfig = QExchangeRateConfig.exchangeRateConfig;

        BooleanBuilder where = new BooleanBuilder();
        where.and(exchangeRateConfig.enabled.eq(true));
        return jpaQueryFactory.selectFrom(exchangeRate)
                              .join(exchangeRateConfig)
                              .on(exchangeRate.source.eq(exchangeRateConfig.source)
                                                           .and(exchangeRate.dest.eq(exchangeRateConfig.dest)))
                              .where(where)
                              .orderBy(exchangeRate.source.asc(), exchangeRate.dest.asc())
                              .fetch();

    }

}
