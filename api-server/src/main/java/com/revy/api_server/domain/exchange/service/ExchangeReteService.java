package com.revy.api_server.domain.exchange.service;

import com.revy.api_server.domain.exchange.ExRate;
import com.revy.api_server.domain.exchange.ExchangeConfig;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ExchangeReteService {
    @Transactional(readOnly = true)
    long getExchangeRateCount();

    @Transactional(readOnly = true)
    long getExchangeRateConfigCount();

    @Transactional
    List<ExRate> saveExchangeRate(List<ExRate> exRates);

    @Transactional
    ExchangeConfig save(ExchangeConfig config);

    @Transactional(readOnly = true)
    List<ExchangeConfig> findAllConfig();

    @Transactional(readOnly = true)
    List<ExRate> getCurrentExchangeRate();
}
