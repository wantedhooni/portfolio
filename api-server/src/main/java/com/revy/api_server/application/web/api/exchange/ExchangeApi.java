package com.revy.api_server.application.web.api.exchange;

import com.revy.api_server.application.web.api.exchange.payload.ExchangeRatePayload;
import com.revy.api_server.application.web.api.exchange.usecase.impl.ExchangeUseCaseImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exchange")
@RequiredArgsConstructor
public class ExchangeApi {

    private final ExchangeUseCaseImpl exchangeUseCaseImpl;

    @GetMapping
    public ExchangeRatePayload.Res getCurrentServiceRates() {
        return exchangeUseCaseImpl.getServiceRate();
    }
}
