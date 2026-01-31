package com.revy.api_server.application.web.api.exchange.usecase;

import com.revy.api_server.application.web.api.exchange.payload.ExchangeRatePayload;

public interface ExchangeUseCase {
    ExchangeRatePayload.Res getServiceRate();
}
