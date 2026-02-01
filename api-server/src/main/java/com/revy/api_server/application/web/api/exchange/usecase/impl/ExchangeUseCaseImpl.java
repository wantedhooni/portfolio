package com.revy.api_server.application.web.api.exchange.usecase.impl;

import com.revy.api_server.application.web.api.exchange.payload.ExchangeRatePayload;
import com.revy.api_server.application.web.api.exchange.usecase.ExchangeUseCase;
import com.revy.securities.domain.exchange.ExRate;
import com.revy.securities.domain.exchange.service.ExchangeReteService;
import com.revy.common.enums.Currency;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExchangeUseCaseImpl implements ExchangeUseCase {
    private final ExchangeReteService exchangeReteService;

    /**
     * @return
     */
    @Override
    public ExchangeRatePayload.Res getServiceRate() {
        List<ExRate> result = exchangeReteService.getCurrentExchangeRate();

        Map<Currency, List<ExchangeRatePayload.DestData>> dataMap = result.stream().collect(
                Collectors.groupingBy(ExRate::getSource, Collectors.mapping(
                        rate -> new ExchangeRatePayload.DestData(rate.getId(), rate.getDest(), rate.getRateTime(), rate.getRate()),
                        Collectors.toList() // mapping의 두 번째 인자로 들어가야 합니다
                )));
        return new ExchangeRatePayload.Res(dataMap);
    }
}
