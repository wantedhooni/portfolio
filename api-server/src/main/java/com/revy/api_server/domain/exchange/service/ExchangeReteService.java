package com.revy.api_server.domain.exchange.service;

import com.revy.api_server.domain.exchange.ExchangeRate;
import com.revy.api_server.domain.exchange.ExchangeRateConfig;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ExchangeReteService {
    @Transactional(readOnly = true)
    long getExchangeRateCount();

    @Transactional(readOnly = true)
    long getExchangeRateConfigCount();

    @Transactional
    List<ExchangeRate> saveExchangeRate(List<ExchangeRate> exchangeRates);

    @Transactional
    ExchangeRateConfig save(ExchangeRateConfig config);

    @Transactional(readOnly = true)
    List<ExchangeRateConfig> findAllConfig();

    @Transactional(readOnly = true)
    List<ExchangeRate> getCurrentExchangeRate();
}
