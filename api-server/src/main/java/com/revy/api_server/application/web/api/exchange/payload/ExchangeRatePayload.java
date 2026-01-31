package com.revy.api_server.application.web.api.exchange.payload;

import com.revy.common.enums.Currency;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ExchangeRatePayload {

    @Schema(name = "ExchangeRatePayload.Res")
    public record Res(
            Map<Currency, List<DestData>> rate
    ) {
    }

    @Schema(name = "ExchangeRatePayload.Res.DestData")
    public record DestData(
            UUID id,
            Currency dest,
            LocalDateTime rateTime,
            BigDecimal rate
    ) {
    }
}
